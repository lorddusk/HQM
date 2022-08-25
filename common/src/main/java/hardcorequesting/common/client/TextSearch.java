package hardcorequesting.common.client;

import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A helper class for performing a text search among items (or other things) on a separate thread.
 * It is designed to be used for a gui, and as such is designed to have only one search running at the same time.
 */
public class TextSearch<T> {
    
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    
    private static Future<?> currentSearch;
    
    public static List<SearchEntry<ItemStack>> ITEMS = new ArrayList<>();
    public static List<SearchEntry<FluidStack>> FLUIDS = new ArrayList<>();
    
    public static <A, B> Stream<SearchEntry<B>> innerMap(Stream<SearchEntry<A>> stream, Function<A, B> mapper) {
        return stream.map(entry -> entry.map(mapper));
    }
    
    public static <T> Future<List<T>> startSearch(String text, Supplier<Stream<SearchEntry<T>>> searchEntrySupplier, int limit) {
        if (currentSearch != null)
            currentSearch.cancel(true);
        
        TextSearch<T> search = new TextSearch<>(text, searchEntrySupplier, limit);
        Future<List<T>> future = EXECUTOR.submit(search::doSearch);
        currentSearch = future;
        return future;
    }
    
    private final String text;
    private final Supplier<Stream<SearchEntry<T>>> searchEntrySupplier;
    private final int limit;
    private final boolean advancedTooltips;
    
    private TextSearch(String text, Supplier<Stream<SearchEntry<T>>> searchEntrySupplier, int limit) {
        this.text = text;
        this.searchEntrySupplier = searchEntrySupplier;
        this.limit = limit;
        this.advancedTooltips = Minecraft.getInstance().options.advancedItemTooltips;
    }
    
    private List<T> doSearch() {
        initItems();
        Pattern pattern = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        
        return searchEntrySupplier.get().flatMap(entry -> entry.tryMatch(pattern, advancedTooltips))
                .limit(limit).collect(Collectors.toList());
    }
    
    @SuppressWarnings("rawtypes")
    public static void initItems() {
        if (ITEMS.isEmpty() || FLUIDS.isEmpty()) {
            clear();
            NonNullList<ItemStack> stacks = NonNullList.create();
            for (Item item : Registry.ITEM) {
                try {
                    item.fillItemCategory(item.getItemCategory(), stacks);
                } catch (Exception ignore) {
                }
            }
            Player player = Minecraft.getInstance().player;
            for (ItemStack stack : stacks) {
                List tooltipList = stack.getTooltipLines(player, TooltipFlag.Default.NORMAL);
                List advTooltipList = stack.getTooltipLines(player, TooltipFlag.Default.ADVANCED);
                StringBuilder searchString = new StringBuilder();
                for (Object string : tooltipList) {
                    if (string != null)
                        searchString.append(string).append("\n");
                }
                StringBuilder advSearchString = new StringBuilder();
                for (Object string : advTooltipList) {
                    if (string != null)
                        advSearchString.append(string).append("\n");
                }
                ITEMS.add(new SearchEntry<>(searchString.toString(), advSearchString.toString(), stack));
            }
            for (Fluid fluid : Registry.FLUID) {
                if (fluid instanceof EmptyFluid) continue;
                if (!fluid.defaultFluidState().isSource()) continue;
                FluidStack fluidVolume = FluidStack.create(fluid, HardcoreQuestingCore.platform.getBucketAmount().intValue());
                String search = fluidVolume.getName().getString();
                FLUIDS.add(new SearchEntry<>(search, search, fluidVolume));
            }
        }
    }
    
    public static void clear() {
        FLUIDS.clear();
        ITEMS.clear();
    }
    
    public static class SearchEntry<T> {
        private final String toolTip;
        private final String advToolTip;
        private final T element;
        
        public SearchEntry(String searchString, String advSearchString, T element) {
            this.toolTip = searchString;
            this.advToolTip = advSearchString;
            this.element = element;
        }
        
        public Stream<T> tryMatch(Pattern pattern, boolean advanced) {
            if (pattern.matcher(advanced ? advToolTip : toolTip).find()) {
                return Stream.of(element);
            } else {
                return Stream.empty();
            }
        }
        
        public <R> SearchEntry<R> map(Function<T, R> mapper) {
            return new SearchEntry<>(toolTip, advToolTip, mapper.apply(element));
        }
    }
}
