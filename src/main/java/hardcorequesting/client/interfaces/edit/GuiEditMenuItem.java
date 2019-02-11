package hardcorequesting.client.interfaces.edit;

import java.util.*;
import java.util.regex.Pattern;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.client.interfaces.TextBoxGroup;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem.Search.ThreadingHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.util.SaveHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiEditMenuItem extends GuiEditMenu {

    private static final int ARROW_X_LEFT = 20;
    private static final int ARROW_X_RIGHT = 150;
    private static final int ARROW_Y = 40;
    private static final int ARROW_SRC_X = 244;
    private static final int ARROW_SRC_Y = 176;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 10;
    private static final int PLAYER_X = 20;
    private static final int PLAYER_Y = 80;
    private static final int SEARCH_X = 180;
    private static final int SEARCH_Y = 30;
    private static final int SIZE = 18;
    private static final int OFFSET = 20;
    private static final int ITEMS_PER_LINE = 7;
    private static final int SEARCH_LINES = 9;
    private static final int ITEMS_TO_DISPLAY = SEARCH_LINES * ITEMS_PER_LINE;
    private static final int PLAYER_LINES = 6;
    public static ThreadingHandler HANDLER = new ThreadingHandler();
    protected Element<?> selected;
    private int id;
    private UUID questId;
    private Type type;
    private List<Element<?>> playerItems;
    public List<Element<?>> searchItems;
    private ItemPrecision precision;
    private boolean clicked;
    private TextBoxGroup.TextBox amountTextBox;
    private TextBoxGroup textBoxes;


    public GuiEditMenuItem(GuiBase gui, EntityPlayer player, Object obj, int id, Type type, int amount, ItemPrecision precision) {
        this(gui, player, Element.create(obj), null, id, type, amount, precision);
    }

    public GuiEditMenuItem(GuiBase gui, EntityPlayer player, Object obj, UUID questId, Type type, int amount, ItemPrecision precision) {
        this(gui, player, Element.create(obj), questId, -1, type, amount, precision);
    }

    public GuiEditMenuItem(GuiBase gui, EntityPlayer player, Element<?> element, UUID questId, int id, final Type type, final int amount, ItemPrecision precision) {
        super(gui, player, true);
        this.selected = element;
        this.id = id;
        this.questId = questId;
        this.type = type;
        this.precision = precision;
        
        playerItems = new ArrayList<>();
        searchItems = new ArrayList<>();
        InventoryPlayer inventory = Minecraft.getMinecraft().player.inventory;
        int itemLength = inventory.getSizeInventory();
        for (int i = 0; i < itemLength; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stack = stack.copy();
                stack.setCount(1);
                boolean exists = false;
                for (Element<?> other : playerItems) {
                    if (ItemStack.areItemStacksEqual(stack, (ItemStack) other.getStack())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists && stack.getItem() != ModItems.book) {
                    playerItems.add(new ElementItem(stack));
                }
            }
        }
        if (type.allowFluids) {
            List<String> fluids = new ArrayList<>();
            int end = playerItems.size();
            for (int i = 0; i < end; i++) {
                Element<?> item = playerItems.get(i);
                ItemStack stack = (ItemStack) item.getStack();
                if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, EnumFacing.NORTH)) {
                    FluidStack fluidStack = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, EnumFacing.NORTH).drain(0, false);
                    if (fluidStack != null && !fluids.contains(fluidStack.getFluid().getName())) {
                        fluids.add(fluidStack.getFluid().getName());
                        playerItems.add(new ElementFluid(fluidStack.getFluid()));
                        if (playerItems.size() == PLAYER_LINES * ITEMS_PER_LINE) {
                            break;
                        }
                    }
                }
            }
        }

        textBoxes = new TextBoxGroup();
        if (type.allowAmount) {
            textBoxes.add(amountTextBox = new TextBoxGroup.TextBox(gui, String.valueOf(amount), 100, 18, false) {
                @Override
                protected boolean isCharacterValid(char c) {
                    return Character.isDigit(c);
                }

                @Override
                public void textChanged(GuiBase gui) {
                    try {
                        int number;
                        if (getText().equals("")) {
                            number = 1;
                        } else {
                            number = Integer.parseInt(getText());
                        }

                        if (number == 0) {
                            number = 1;
                        }

                        if (getSelected() != null) {
                            getSelected().setAmount(number);
                        }
                    } catch (Exception ignored) {
                    }

                }
            });
        }
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 230, 18, false){
            @Override
            public void textChanged(GuiBase gui) {
                searchItems.clear();
                Thread thread = new Thread(new Search(getText(), GuiEditMenuItem.this));
                thread.start();
            }
        });
    }

    private boolean inArrowBounds(GuiBase gui, int mX, int mY, boolean left) {
        return gui.inBounds(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, ARROW_W, ARROW_H, mX, mY);
    }

    private void drawArrow(GuiBase gui, int mX, int mY, boolean left) {
        int srcX = ARROW_SRC_X + (left ? 0 : ARROW_W);
        int srcY = ARROW_SRC_Y + (inArrowBounds(gui, mX, mY, left) ? clicked ? 1 : 2 : 0) * ARROW_H;

        gui.drawRect(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, srcX, srcY, ARROW_W, ARROW_H);
    }

    private boolean usePrecision() {
        return type.allowPrecision && selected instanceof ElementItem;
    }

    public boolean showFluids() {
        return type.allowFluids;
    }

    private Element<?> getSelected() {
        return selected;
    }

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);
        gui.drawString("Selected", 20, 20, 0x404040);
        selected.draw(gui, 70, 15, mX, mY);
        gui.drawString("Search", 180, 20, 0x404040);
        drawList(gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);

        gui.drawString("Player inventory", 20, 70, 0x404040);
        drawList(gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);

        textBoxes.draw(gui);

        if (usePrecision()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            drawArrow(gui, mX, mY, true);
            drawArrow(gui, mX, mY, false);
            gui.drawCenteredString(precision.getName(), ARROW_X_LEFT + ARROW_W, ARROW_Y, 0.7F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
        }

    }

    @Override
    public void drawMouseOver(GuiBase gui, int mX, int mY) {
        super.drawMouseOver(gui, mX, mY);

        drawListMouseOver(gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);
        drawListMouseOver(gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);

        clickList(gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);
        clickList(gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);

        textBoxes.onClick(gui, mX, mY);

        if (usePrecision()) {
            if (inArrowBounds(gui, mX, mY, true)) {
                List<ItemPrecision> precisionTypes = ItemPrecision.getPrecisionTypes();
                precision = precisionTypes.get((precisionTypes.indexOf(precision) + precisionTypes.size() - 1) % precisionTypes.size());
                clicked = true;
            } else if (inArrowBounds(gui, mX, mY, false)) {
                List<ItemPrecision> precisionTypes = ItemPrecision.getPrecisionTypes();
                precision = precisionTypes.get((precisionTypes.indexOf(precision) + 1) % precisionTypes.size());
                clicked = true;
            }
        }
    }

    @Override
    public void onKeyTyped(GuiBase gui, char c, int k) {
        super.onKeyTyped(gui, c, k);

        textBoxes.onKeyStroke(gui, c, k);
    }

    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);
        clicked = false;
    }

    @Override
    public void save(GuiBase gui) {
        if (type == Type.BAG_ITEM) {
            if (GuiQuestBook.getSelectedGroup() != null && selected instanceof ElementItem && selected.getStack() != null && !selected.isEmpty()) {
                GuiQuestBook.getSelectedGroup().setItem(id, (ItemStack) selected.getStack());
            }
        } else if (type == Type.QUEST_ICON) {
            if (Quest.getQuest(questId) != null && selected instanceof ElementItem && !selected.isEmpty()) {
                try {
                    Quest.getQuest(questId).setIconStack((ItemStack) selected.getStack());
                } catch (Exception e) {
                    System.out.println("Tell LordDusk that he found the issue.");
                }
                SaveHelper.add(SaveHelper.EditType.ICON_CHANGE);
            }
        } else {
            if (!selected.isEmpty()) {
                GuiQuestBook.selectedQuest.setItem(selected, id, type, precision, player);
            }
        }
    }

    private void drawList(GuiBase gui, int x, int y, List<Element<?>> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element<?> element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;

            element.draw(gui, x + xI * OFFSET, y + yI * OFFSET, mX, mY);
        }
    }

    private void drawListMouseOver(GuiBase gui, int x, int y, List<Element<?>> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element<? >element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;

            if (gui.inBounds(x + xI * OFFSET, y + yI * OFFSET, SIZE, SIZE, mX, mY)) {
                if (element != null) {
                    gui.drawMouseOver(element.getName(gui), mX + gui.getLeft(), mY + gui.getTop());
                }
                break;
            }

        }
    }

    private void clickList(GuiBase gui, int x, int y, List<Element<?>> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element<?> element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;

            if (gui.inBounds(x + xI * OFFSET, y + yI * OFFSET, SIZE, SIZE, mX, mY)) {
                if (element != null) {
                    selected = element.copy();
                    if (amountTextBox != null) {
                        amountTextBox.textChanged(gui);
                    } else {
                        selected.setAmount(1);
                    }
                }
                break;
            }
        }

    }

    public enum Type {
        REWARD(false, true, false),
        PICK_REWARD(false, true, false),
        CONSUME_TASK(true, true, true),
        CRAFTING_TASK(false, true, true),
        QUEST_ICON(false, false, false),
        BAG_ITEM(false, true, false),
        LOCATION(false, false, false),
        TAME(false, false, false),
        MOB(false, false, false),
        ADVANCEMENT(false, false, false),
        PORTAL(false, false, false),
        BLOCK_PLACE_TASK(false, true, true),
        BLOCK_BREAK_TASK(false, true, true);

        private boolean allowFluids;
        private boolean allowAmount;
        private boolean allowPrecision;

        Type(boolean allowFluids, boolean allowAmount, boolean allowPrecision) {
            this.allowFluids = allowFluids;
            this.allowAmount = allowAmount;
            this.allowPrecision = allowPrecision;
        }
    }

    public static abstract class Element<T> {

        protected T stack;

        protected Element() {
        }

        public abstract void draw(GuiBase gui, int x, int y, int mX, int mY);

        public abstract List<String> getName(GuiBase gui);

        public abstract int getAmount();

        public abstract void setAmount(int val);

        public T getStack() {
            return stack;
        }

        public abstract Element<?> copy();

        public abstract boolean isEmpty();

        public static Element<?> create (Object stack) {
            if (stack instanceof ItemStack) return new ElementItem((ItemStack) stack);
            else if (stack instanceof FluidStack) return new ElementFluid((Fluid) stack);
            else return null;
        }
    }

    public static class ElementItem extends Element<ItemStack> {

        public ElementItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void draw(GuiBase gui, int x, int y, int mX, int mY) {
            if (stack != null && !stack.isEmpty()) {
                gui.drawItemStack(stack, x, y, mX, mY, false);
            }
        }

        @Override
        public List<String> getName(GuiBase gui) {
            if (stack != null && !stack.isEmpty()) {
                return stack.getTooltip(Minecraft.getMinecraft().player, gui.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
            } else {
                List<String> ret = new ArrayList<>();
                ret.add("Unknown");
                return ret;
            }
        }

        @Override
        public int getAmount() {
            return (stack == null || stack.isEmpty()) ? 0 : stack.getCount();
        }

        @Override
        public void setAmount(int val) {
            if (stack != null && !stack.isEmpty()) {
                stack.setCount(val);
            }
        }

        @Override
        public Element<?> copy() {
            return new ElementItem((stack == null || stack.isEmpty()) ? null : stack.copy());
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

    }

    public static class ElementFluid extends Element<Fluid> {

        private int size;

        public ElementFluid(Fluid fluid) {
            this.stack = fluid;
        }

        @Override
        public void draw(GuiBase gui, int x, int y, int mX, int mY) {
            gui.drawFluid(stack, x, y, mX, mY);
        }

        @Override
        public List<String> getName(GuiBase gui) {
            if(stack != null){
                List<String> ret = new ArrayList<>();
                ret.add(stack.getLocalizedName(new FluidStack(stack, 1000)));
                return ret;
            }
            return Collections.emptyList();
        }

        @Override
        public int getAmount() {
            return size;
        }

        @Override
        public void setAmount(int val) {
            size = val;
        }

        @Override
        public Element<?> copy() {
            ElementFluid ret = new ElementFluid(stack);
            ret.size = size;
            return ret;
        }

        @Override
        // I don't think this is technically ever really empty
        public boolean isEmpty() {
            return false;
        }
}

    public static class Search implements Runnable {

        public static List<SearchEntry> searchItems = new ArrayList<>();
        public static List<SearchEntry> searchFluids = new ArrayList<>();

        private String search;
        private GuiEditMenuItem menu;
        public List<Element<?>> elements;
        private long startTime;

        public Search(String search, GuiEditMenuItem menu) {
            this.search = search;
            this.menu = menu;
            startTime = System.currentTimeMillis();
        }

        public static void setResult(GuiEditMenuItem menu, Search search) {
            ThreadingHandler.handle(menu, search);
        }

        @SuppressWarnings("rawtypes")
        public static void initItems() {
            clear();
            if (searchItems.isEmpty()) {
                NonNullList<ItemStack> stacks = NonNullList.create();
                for (Item item : Item.REGISTRY) {
                    try {
                        item.getSubItems(item.getCreativeTab(), stacks);
                    } catch (Exception ignore) {
                    }
                }
                EntityPlayer player = Minecraft.getMinecraft().player;
                for (ItemStack stack : stacks) {
                        List tooltipList = stack.getTooltip(player, ITooltipFlag.TooltipFlags.NORMAL);
                        List advTooltipList = stack.getTooltip(player, ITooltipFlag.TooltipFlags.ADVANCED);
                        String searchString = "";
                        for (Object string : tooltipList) {
                            if (string != null)
                                searchString += string + "\n";
                        }
                        String advSearchString = "";
                        for (Object string : advTooltipList) {
                            if (string != null)
                                advSearchString += string + "\n";
                        }
                        searchItems.add(new SearchEntry(searchString, advSearchString, new ElementItem(stack)));
                }
                for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
                    String search = fluid.getLocalizedName(new FluidStack(fluid, 1000)); // Dummy Fluid Stack
                    searchFluids.add(new SearchEntry(search, search, new ElementFluid(fluid)));
                }
            }
        }

        public static void clear() {
            searchFluids.clear();
            searchItems.clear();
        }

        @Override
        public void run() {
            elements = new ArrayList<>();
            Pattern pattern = Pattern.compile(Pattern.quote(search), Pattern.CASE_INSENSITIVE);
            boolean advanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;
            for (int i = 0; i < searchItems.size() && elements.size() < ITEMS_TO_DISPLAY; i++) {
                SearchEntry entry = searchItems.get(i);
                entry.search(pattern, elements, advanced);
            }
            if (menu.showFluids()) {
                for (int i = 0; i < searchFluids.size() && elements.size() < ITEMS_TO_DISPLAY; i++) {
                    SearchEntry entry = searchFluids.get(i);
                    entry.search(pattern, elements, advanced);
                }
            }
            setResult(this.menu, this);
        }

        public boolean isNewerThan(Search search) {
            return startTime > search.startTime;
        }

        public static class SearchEntry {

            private String toolTip;
            private String advToolTip;
            private GuiEditMenuItem.Element<?> element;

            public SearchEntry(String searchString, String advSearchString, GuiEditMenuItem.Element<?> element) {
                this.toolTip = searchString;
                this.advToolTip = advSearchString;
                this.element = element;
            }

            public void search(Pattern pattern, List<Element<?>> elements, boolean advanced) {
                if (pattern.matcher(advanced ? advToolTip : toolTip).find()) {
                    elements.add(element);
                }
            }
        }
        
        public static class ThreadingHandler
        {
            private Map<GuiEditMenuItem, Search> handle = new LinkedHashMap<>();

            private ThreadingHandler() {
                MinecraftForge.EVENT_BUS.register(this);
            }

            private static void handle(GuiEditMenuItem menu, Search search) {
                if (!HANDLER.handle.containsKey(menu) || search.isNewerThan(HANDLER.handle.get(menu)))
                    HANDLER.handle.put(menu, search);
            }

            @SubscribeEvent
            public void renderEvent(RenderWorldLastEvent e) {
                if (!handle.isEmpty()) {
                    for (Map.Entry<GuiEditMenuItem, Search> entry : handle.entrySet()) {
                        entry.getKey().searchItems = entry.getValue().elements;
                    }
                    handle.clear();
                }
            }
        }
    }
}
