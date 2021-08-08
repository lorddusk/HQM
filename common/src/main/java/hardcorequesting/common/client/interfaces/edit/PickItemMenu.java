package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PickItemMenu<T> extends GuiEditMenu {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
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
    
    private final Consumer<Result<T>> resultConsumer;
    private final boolean precisionInput;
    private final Type<T> type;
    
    private final List<T> playerItems;
    private List<T> searchItems;
    private Future<List<T>> search;
    
    private T selected;
    private int amount;
    private ItemPrecision precision;
    private boolean clicked;
    private TextBoxGroup.TextBox amountTextBox;
    private TextBoxGroup textBoxes;
    private int lastClicked;
    
    public static <T> void display(GuiBase gui, Player player, T initial, Type<T> type, Consumer<Result<T>> resultConsumer) {
        gui.setEditMenu(new PickItemMenu<>(gui, player, initial, type, 1, false, ItemPrecision.PRECISE, false, resultConsumer));
    }
    
    public static <T> void display(GuiBase gui, Player player, T obj, Type<T> type, int amount, Consumer<Result<T>> resultConsumer) {
        gui.setEditMenu(new PickItemMenu<>(gui, player, obj, type, amount, true, ItemPrecision.PRECISE, false, resultConsumer));
    }
    
    public static <T> void display(GuiBase gui, Player player, T obj, Type<T> type, int amount, ItemPrecision precision, Consumer<Result<T>> resultConsumer) {
        gui.setEditMenu(new PickItemMenu<>(gui, player, obj, type, amount, true, precision, true, resultConsumer));
    }
    
    private PickItemMenu(GuiBase gui, Player player, T element, final Type<T> type, final int amount, boolean amountInput, ItemPrecision precision, boolean precisionInput, Consumer<Result<T>> resultConsumer) {
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
                        
                    } catch (Exception ignored) {
                    }
                    
                }
            });
        }
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 230, 18, false) {
            @Override
            public void textChanged(GuiBase gui) {
                startSearch(getText());
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
        return precisionInput && type.mayHavePrecision(selected);
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        checkSearchResult();
        
        super.draw(matrices, gui, mX, mY);
        gui.drawString(matrices, Translator.plain("Selected"), 20, 20, 0x404040);
        type.draw(selected, matrices, gui, 70, 15, mX, mY);
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
        if (!type.isEmpty(selected)) {
            resultConsumer.accept(new Result<>(type.copyWith(selected, amount), amount, precision));
        }
    }
    
    private void drawList(PoseStack matrices, GuiBase gui, int x, int y, List<T> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            T element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;
    
            type.draw(element, matrices, gui, x + xI * OFFSET, y + yI * OFFSET, mX, mY);
        }
    }
    
    private void drawListMouseOver(PoseStack matrices, GuiBase gui, int x, int y, List<T> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            T element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;
            
            if (gui.inBounds(x + xI * OFFSET, y + yI * OFFSET, SIZE, SIZE, mX, mY)) {
                if (element != null) {
                    gui.renderTooltipL(matrices, type.getName(element, gui), mX + gui.getLeft(), mY + gui.getTop());
                }
                break;
            }
            
        }
    }
    
    private boolean clickList(GuiBase gui, int x, int y, List<T> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            T element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;
            
            if (gui.inBounds(x + xI * OFFSET, y + yI * OFFSET, SIZE, SIZE, mX, mY)) {
                if (element != null) {
                    selected = element;
                    
                    int lastDiff = player.tickCount - lastClicked;
                    if (0 <= lastDiff && lastDiff < 6 && !type.isEmpty(selected)) {
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
    
    public static abstract class Type<T> {
        public static final Type<ItemStack> ITEM = new Type<ItemStack>() {
            @Override
            protected List<ItemStack> createPlayerEntries(Player player) {
                return getPlayerItems(player);
            }
    
            @Override
            protected Stream<TextSearch.SearchEntry<ItemStack>> getSearchEntriesStream() {
                return TextSearch.ITEMS.stream();
            }
    
            @Override
            protected void draw(ItemStack item, PoseStack matrices, GuiBase gui, int x, int y, int mX, int mY) {
                gui.drawItemStack(item, x, y, mX, mY, false);
            }
    
            @Override
            protected List<Component> getName(ItemStack item, GuiBase gui) {
                return gui.getTooltipFromItem(item);
            }
    
            @Override
            protected boolean isEmpty(ItemStack item) {
                return item.isEmpty();
            }
    
            @Override
            protected boolean mayHavePrecision(ItemStack item) {
                return true;
            }
    
            @Override
            protected ItemStack copyWith(ItemStack item, int amount) {
                ItemStack newStack = item.copy();
                newStack.setCount(amount);
                return newStack;
            }
        };
        
        public static final Type<Either<ItemStack, FluidStack>> ITEM_FLUID = new Type<Either<ItemStack, FluidStack>>() {
            @Override
            protected List<Either<ItemStack, FluidStack>> createPlayerEntries(Player player) {
                return Stream.concat(getPlayerItems(player).stream().map(Either::<ItemStack, FluidStack>left),
                        getPlayerFluids(player).stream().map(Either::<ItemStack, FluidStack>right)
                ).collect(Collectors.toList());
            }
    
            @Override
            protected Stream<TextSearch.SearchEntry<Either<ItemStack, FluidStack>>> getSearchEntriesStream() {
                return Stream.concat(TextSearch.innerMap(TextSearch.ITEMS.stream(), Either::<ItemStack, FluidStack>left),
                        TextSearch.innerMap(TextSearch.FLUIDS.stream(), Either::<ItemStack, FluidStack>right));
            }
    
            @Override
            protected void draw(Either<ItemStack, FluidStack> item, PoseStack matrices, GuiBase gui, int x, int y, int mX, int mY) {
                item.ifLeft(stack -> gui.drawItemStack(stack, x, y, mX, mY, false))
                        .ifRight(stack -> gui.drawFluid(stack, matrices, x, y, mX, mY));
            }
    
            @Override
            protected List<Component> getName(Either<ItemStack, FluidStack> item, GuiBase gui) {
                return item.map(gui::getTooltipFromItem,
                        stack -> Collections.singletonList(stack.getName()));
            }
    
            @Override
            protected boolean isEmpty(Either<ItemStack, FluidStack> item) {
                return item.map(ItemStack::isEmpty, FluidStack::isEmpty);
            }
    
            @Override
            protected boolean mayHavePrecision(Either<ItemStack, FluidStack> item) {
                return item.left().isPresent();
            }
    
            @Override
            protected Either<ItemStack, FluidStack> copyWith(Either<ItemStack, FluidStack> item, int amount) {
                return item.mapBoth(stack -> {
                    ItemStack newStack = stack.copy();
                    newStack.setCount(amount);
                    return newStack;
                }, stack -> HardcoreQuestingCore.platform.createFluidStack(stack.getFluid(), Fraction.ofWhole(amount)));
            }
        };
        
        protected abstract List<T> createPlayerEntries(Player player);
    
        protected abstract Stream<TextSearch.SearchEntry<T>> getSearchEntriesStream();
    
        protected abstract void draw(T item, PoseStack matrices, GuiBase gui, int x, int y, int mX, int mY);
    
        protected abstract List<? extends FormattedText> getName(T item, GuiBase gui);
        
        protected abstract boolean isEmpty(T item);
        
        protected abstract boolean mayHavePrecision(T item);
        
        protected abstract T copyWith(T item, int amount);
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
    
    private void startSearch(String text) {
        searchItems.clear();
        search = TextSearch.startSearch(text, type::getSearchEntriesStream, ITEMS_TO_DISPLAY);
    }
    
    private void checkSearchResult() {
        if(search != null && search.isDone()) {
            if(search.isCancelled()) {
                LOGGER.error("Item search had been cancelled, but the reference was kept!");
                search = null;
            } else {
                try {
                    searchItems = search.get();
                    search = null;
                } catch (ExecutionException e) {
                    LOGGER.error("Item search failed with error: ", e.getCause());
                    search = null;
                } catch (InterruptedException ignored) {
                }
            }
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
        }
        
        public T get() {
            return value;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public ItemPrecision getPrecision() {
            return precision;
        }
    }
}
