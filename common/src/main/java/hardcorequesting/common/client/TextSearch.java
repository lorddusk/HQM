package hardcorequesting.common.client;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.platform.FluidStack;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextSearch {
    
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    
    private static Future<?> currentSearch;
    
    public static List<SearchEntry> searchItems = new ArrayList<>();
    public static List<SearchEntry> searchFluids = new ArrayList<>();
    
    public static Future<List<PickItemMenu.Element<?>>> startSearch(String text, PickItemMenu menu) {
        if (currentSearch != null)
            currentSearch.cancel(true);
        
        TextSearch search = new TextSearch(text, menu);
        Future<List<PickItemMenu.Element<?>>> future = EXECUTOR.submit(search::doSearch);
        currentSearch = future;
        return future;
    }
    
    private String search;
    private PickItemMenu menu;
    
    private TextSearch(String search, PickItemMenu menu) {
        this.search = search;
        this.menu = menu;
    }
    
    private List<PickItemMenu.Element<?>> doSearch() {
        initItems();
        Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
        boolean advanced = Minecraft.getInstance().options.advancedItemTooltips;
        
        return menu.type.getSearchEntriesStream().flatMap(entry -> entry.tryMatch(pattern, advanced))
                .limit(PickItemMenu.ITEMS_TO_DISPLAY).collect(Collectors.toList());
    }
    
    @SuppressWarnings("rawtypes")
    public static void initItems() {
        if (searchItems.isEmpty() || searchFluids.isEmpty()) {
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
                searchItems.add(new SearchEntry(searchString.toString(), advSearchString.toString(), new PickItemMenu.ElementItem(stack)));
            }
            for (Fluid fluid : Registry.FLUID) {
                if (fluid instanceof EmptyFluid) continue;
                if (!fluid.defaultFluidState().isSource()) continue;
                FluidStack fluidVolume = HardcoreQuestingCore.platform.createFluidStack(fluid, HardcoreQuestingCore.platform.getBucketAmount());
                String search = fluidVolume.getName().getString();
                searchFluids.add(new SearchEntry(search, search, new PickItemMenu.ElementFluid(fluidVolume)));
            }
        }
    }
    
    public static void clear() {
        searchFluids.clear();
        searchItems.clear();
    }
    
    public static class SearchEntry {
        private String toolTip;
        private String advToolTip;
        private PickItemMenu.Element<?> element;
        
        public SearchEntry(String searchString, String advSearchString, PickItemMenu.Element<?> element) {
            this.toolTip = searchString;
            this.advToolTip = advSearchString;
            this.element = element;
        }
        
        public Stream<PickItemMenu.Element<?>> tryMatch(Pattern pattern, boolean advanced) {
            if (pattern.matcher(advanced ? advToolTip : toolTip).find()) {
                return Stream.of(element);
            } else {
                return Stream.empty();
            }
        }
    }
}
