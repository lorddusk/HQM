package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.TextSearch;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.TextBoxGroup;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PickItemMenu extends GuiEditMenu {
    
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
    public static final int ITEMS_TO_DISPLAY = SEARCH_LINES * ITEMS_PER_LINE;
    private static final int PLAYER_LINES = 6;
    
    private final Consumer<Result<?>> resultConsumer;
    private final boolean precisionInput;
    public final Type type;
    
    private final List<Element<?>> playerItems;
    public List<Element<?>> searchItems;
    
    private Element<?> selected;
    private int amount;
    private ItemPrecision precision;
    private boolean clicked;
    private TextBoxGroup.TextBox amountTextBox;
    private TextBoxGroup textBoxes;
    private int lastClicked;
    
    public static void display(GuiBase gui, Player player, Object obj, Type type, Consumer<Result<?>> resultConsumer) {
        gui.setEditMenu(new PickItemMenu(gui, player, Element.create(obj), type, 1, false, ItemPrecision.PRECISE, false, resultConsumer));
    }
    
    public static void display(GuiBase gui, Player player, Object obj, Type type, int amount, Consumer<Result<?>> resultConsumer) {
        gui.setEditMenu(new PickItemMenu(gui, player, Element.create(obj), type, amount, true, ItemPrecision.PRECISE, false, resultConsumer));
    }
    
    public static void display(GuiBase gui, Player player, Object obj, Type type, int amount, ItemPrecision precision, Consumer<Result<?>> resultConsumer) {
        gui.setEditMenu(new PickItemMenu(gui, player, Element.create(obj), type, amount, true, precision, true, resultConsumer));
    }
    
    private PickItemMenu(GuiBase gui, Player player, Element<?> element, final Type type, final int amount, boolean amountInput, ItemPrecision precision, boolean precisionInput, Consumer<Result<?>> resultConsumer) {
        super(gui, player, true);
        this.resultConsumer = resultConsumer;
        this.type = type;
        this.precisionInput = precisionInput;
        
        this.selected = element;
        this.amount = amount;
        this.precision = precision;
        
        searchItems = Collections.emptyList();
        
        playerItems = type.createPlayerEntries(player);
        
        textBoxes = new TextBoxGroup();
        if (amountInput) {
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
                        
                        PickItemMenu.this.amount = number;
                        if (getSelected() != null) {
                            getSelected().setAmount(number);
                        }
                    } catch (Exception ignored) {
                    }
                    
                }
            });
        }
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 230, 18, false) {
            @Override
            public void textChanged(GuiBase gui) {
                searchItems.clear();
                Thread thread = new Thread(new TextSearch(getText(), PickItemMenu.this));
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
        return precisionInput && selected instanceof ElementItem;
    }
    
    private Element<?> getSelected() {
        return selected;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        gui.drawString(matrices, Translator.plain("Selected"), 20, 20, 0x404040);
        selected.draw(matrices, gui, 70, 15, mX, mY);
        gui.drawString(matrices, Translator.plain("Search"), 180, 20, 0x404040);
        drawList(matrices, gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);
        
        gui.drawString(matrices, Translator.plain("Player inventory"), 20, 70, 0x404040);
        drawList(matrices, gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);
        
        textBoxes.draw(matrices, gui);
        
        if (usePrecision()) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            drawArrow(gui, mX, mY, true);
            drawArrow(gui, mX, mY, false);
            gui.drawCenteredString(matrices, Translator.plain(precision.getName()), ARROW_X_LEFT + ARROW_W, ARROW_Y, 0.7F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
        }
    }
    
    @Override
    public void renderTooltip(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.renderTooltip(matrices, gui, mX, mY);
        
        drawListMouseOver(matrices, gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);
        drawListMouseOver(matrices, gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        if (clickList(gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY)) return;
        if (clickList(gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY)) return;
        
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
    public void onKeyStroke(GuiBase gui, char c, int k) {
        super.onKeyStroke(gui, c, k);
        
        textBoxes.onKeyStroke(gui, c, k);
    }
    
    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);
        clicked = false;
    }
    
    @Override
    public void save(GuiBase gui) {
        if (!selected.isEmpty()) {
            resultConsumer.accept(new Result<>(selected.getStack(), amount, precision));
        }
    }
    
    private void drawList(PoseStack matrices, GuiBase gui, int x, int y, List<Element<?>> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element<?> element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;
            
            element.draw(matrices, gui, x + xI * OFFSET, y + yI * OFFSET, mX, mY);
        }
    }
    
    private void drawListMouseOver(PoseStack matrices, GuiBase gui, int x, int y, List<Element<?>> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element<?> element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;
            
            if (gui.inBounds(x + xI * OFFSET, y + yI * OFFSET, SIZE, SIZE, mX, mY)) {
                if (element != null) {
                    gui.renderTooltipL(matrices, element.getName(gui), mX + gui.getLeft(), mY + gui.getTop());
                }
                break;
            }
            
        }
    }
    
    private boolean clickList(GuiBase gui, int x, int y, List<Element<?>> items, int mX, int mY) {
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
                    int lastDiff = player.tickCount - lastClicked;
                    if (lastDiff < 0) {
                        lastClicked = player.tickCount;
                    } else if (lastDiff < 6 && !selected.isEmpty()) {
                        save(gui);
                        close(gui);
                        return true;
                    } else {
                        lastClicked = player.tickCount;
                    }
                }
                break;
            }
        }
        return false;
    }
    
    public static abstract class Type {
        public static final Type ITEM = new Type() {
            @Override
            protected List<Element<?>> createPlayerEntries(Player player) {
                return getPlayerItems(player).stream().map(ElementItem::new).collect(Collectors.toList());
            }
    
            @Override
            public Stream<TextSearch.SearchEntry> getSearchEntriesStream() {
                return TextSearch.searchItems.stream();
            }
        };
        
        public static final Type ITEM_FLUID = new Type() {
            @Override
            protected List<Element<?>> createPlayerEntries(Player player) {
                return Stream.concat(getPlayerItems(player).stream().map(ElementItem::new),
                        getPlayerFluids(player).stream().map(ElementFluid::new)).collect(Collectors.toList());
            }
    
            @Override
            public Stream<TextSearch.SearchEntry> getSearchEntriesStream() {
                return Stream.concat(TextSearch.searchItems.stream(), TextSearch.searchFluids.stream());
            }
        };
        
        protected abstract List<Element<?>> createPlayerEntries(Player player);
        
        public abstract Stream<TextSearch.SearchEntry> getSearchEntriesStream();
    }
    
    private static List<ItemStack> getPlayerItems(Player player) {
        List<ItemStack> playerItems = new ArrayList<>();
        
        Inventory inventory = player.inventory;
        int itemLength = inventory.getContainerSize();
        for (int i = 0; i < itemLength; i++) {
            ItemStack invStack = inventory.getItem(i);
            if (!invStack.isEmpty() && invStack.getItem() != ModItems.book.get()) {
                ItemStack stack = invStack.copy();
                stack.setCount(1);
                
                if (playerItems.stream().noneMatch(other -> ItemStack.matches(stack, other))) {
                    playerItems.add(stack);
                }
            }
        }
        
        return playerItems;
    }
    
    private static List<FluidStack> getPlayerFluids(Player player) {
        List<FluidStack> playerFluids = new ArrayList<>();
        Set<Fluid> fluids = new HashSet<>();
        
        Inventory inventory = player.inventory;
        int itemLength = inventory.getContainerSize();
        for (int i = 0; i < itemLength; i++) {
            ItemStack stack = inventory.getItem(i);
            
            FluidStack fluid = HardcoreQuestingCore.platform.findFluidIn(stack);
            if (!fluid.isEmpty() && fluids.add(fluid.getFluid())) {
                playerFluids.add(fluid);
            }
        }
        
        return playerFluids;
    }
    
    public static abstract class Element<T> {
        
        protected T stack;
        
        protected Element() {
        }
        
        public abstract void draw(PoseStack matrices, GuiBase gui, int x, int y, int mX, int mY);
        
        public abstract List<FormattedText> getName(GuiBase gui);
    
        public abstract void setAmount(int val);
        
        public T getStack() {
            return stack;
        }
        
        public abstract Element<?> copy();
        
        public abstract boolean isEmpty();
        
        public static Element<?> create(Object stack) {
            if (stack instanceof ItemStack) return new ElementItem((ItemStack) stack);
            else if (stack instanceof FluidStack) return new ElementFluid((FluidStack) stack);
            else return null;
        }
    }
    
    public static class ElementItem extends Element<ItemStack> {
        public ElementItem(ItemStack stack) {
            this.stack = stack;
        }
        
        @Override
        public void draw(PoseStack matrices, GuiBase gui, int x, int y, int mX, int mY) {
            if (stack != null && !stack.isEmpty()) {
                gui.drawItemStack(stack, x, y, mX, mY, false);
            }
        }
        
        @Override
        public List<FormattedText> getName(GuiBase gui) {
            if (stack != null && !stack.isEmpty()) {
                return (List) gui.getTooltipFromItem(stack);
            } else {
                return Collections.singletonList(Translator.plain("Unknown"));
            }
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
            return stack == null || stack.isEmpty();
        }
        
    }
    
    public static class ElementFluid extends Element<FluidStack> {
        
        public ElementFluid(FluidStack fluid) {
            this.stack = fluid;
        }
        
        @Override
        public void draw(PoseStack matrices, GuiBase gui, int x, int y, int mX, int mY) {
            gui.drawFluid(stack, matrices, x, y, mX, mY);
        }
        
        @Override
        public List<FormattedText> getName(GuiBase gui) {
            if (stack != null) {
                return Collections.singletonList(stack.getName());
            }
            return Collections.emptyList();
        }
    
        @Override
        public void setAmount(int val) {
            stack = HardcoreQuestingCore.platform.createFluidStack(stack.getFluid(), Fraction.ofWhole(val));
        }
        
        @Override
        public Element<?> copy() {
            return new ElementFluid(stack.copy());
        }
        
        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }
    
    public static class Result<T> {
        private final T value;
        private final int amount;
        private final ItemPrecision precision;
    
        private Result(T value, int amount, ItemPrecision precision) {
            this.value = value;
            this.amount = amount;
            this.precision = precision;
            if (value instanceof ItemStack && ((ItemStack) value).isEmpty()
                    || value instanceof FluidStack && ((FluidStack) value).isEmpty())
                throw new IllegalArgumentException("Result should be non-empty");
            if(!(value instanceof ItemStack || value instanceof FluidStack))
                throw new IllegalArgumentException("Result must be an item stack or a fluid stack");
        }
    
        public T get() {
            return value;
        }
        
        public ItemStack getStack() {
            return (ItemStack) value;
        }
    
        public int getAmount() {
            return amount;
        }
    
        public ItemPrecision getPrecision() {
            return precision;
        }
        
        public void handle(Consumer<ItemStack> itemConsumer, Consumer<FluidStack> fluidConsumer) {
            if(value instanceof ItemStack) {
                itemConsumer.accept((ItemStack) value);
            } else if(value instanceof  FluidStack) {
                fluidConsumer.accept((FluidStack) value);
            }
        }
    }
}
