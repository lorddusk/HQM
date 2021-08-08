package hardcorequesting.common.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextSearch implements Runnable {
    
    public static final ThreadingHandler HANDLER = new ThreadingHandler();
    
    public static List<SearchEntry> searchItems = new ArrayList<>();
    public static List<SearchEntry> searchFluids = new ArrayList<>();
    
    private String search;
    private PickItemMenu menu;
    public List<PickItemMenu.Element<?>> elements;
    private long startTime;
    
    public TextSearch(String search, PickItemMenu menu) {
        this.search = search;
        this.menu = menu;
        startTime = System.currentTimeMillis();
    }
    
    public static void setResult(PickItemMenu menu, TextSearch search) {
        ThreadingHandler.handle(menu, search);
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
    
    @Override
    public void run() {
        initItems();
        Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
        boolean advanced = Minecraft.getInstance().options.advancedItemTooltips;
        
        elements = menu.type.getSearchEntriesStream().flatMap(entry -> entry.tryMatch(pattern, advanced))
                .limit(PickItemMenu.ITEMS_TO_DISPLAY).collect(Collectors.toList());
        
        setResult(this.menu, this);
    }
    
    public boolean isNewerThan(TextSearch search) {
        return startTime > search.startTime;
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
    
    public static class ThreadingHandler {
        private Map<PickItemMenu, TextSearch> handle = new LinkedHashMap<>();
        
        private ThreadingHandler() {
            HardcoreQuestingCore.platform.registerOnHudRender(this::renderEvent);
        }
        
        private static void handle(PickItemMenu menu, TextSearch search) {
            if (!HANDLER.handle.containsKey(menu) || search.isNewerThan(HANDLER.handle.get(menu)))
                HANDLER.handle.put(menu, search);
        }
        
        public void renderEvent(PoseStack matrices, float delta) {
            if (!handle.isEmpty()) {
                for (Map.Entry<PickItemMenu, TextSearch> entry : handle.entrySet()) {
                    entry.getKey().searchItems = entry.getValue().elements;
                }
                handle.clear();
            }
        }
    }
}
