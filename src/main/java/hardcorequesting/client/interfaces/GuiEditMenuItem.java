package hardcorequesting.client.interfaces;


import hardcorequesting.SaveHelper;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryNamespaced;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiEditMenuItem extends GuiEditMenu {

    public static enum Type {
        REWARD(false, true, false),
        PICK_REWARD(false, true, false),
        CONSUME_TASK(true, true, true),
        CRAFTING_TASK(false, true, true),
        QUEST_ICON(false, false, false),
        BAG_ITEM(false, true, false),
        LOCATION(false, false, false),
        MOB(false, false, false),
        PORTAL(false, false, false);


        private boolean allowFluids;
        private boolean allowAmount;
        private boolean allowPrecision;

        Type(boolean allowFluids, boolean allowAmount, boolean allowPrecision) {
            this.allowFluids = allowFluids;
            this.allowAmount = allowAmount;
            this.allowPrecision = allowPrecision;
        }
    }


    public static abstract class Element <T> {
        protected Element() {}

        protected T item;
        public abstract void draw(GuiBase gui, int x, int y, int mX, int mY);
        public abstract List<String> getName(GuiBase gui);
        public abstract int getAmount();
        public abstract void setAmount(int val);

        public T getItem() {
            return item;
        }
        public abstract Element copy();
    }

    public static class ElementItem extends Element<ItemStack> {
        private ElementItem(ItemStack item) {
            this.item = item;
        }

        @Override
        public void draw(GuiBase gui, int x, int y, int mX, int mY) {
            gui.drawItem(item, x, y, mX, mY, false);
        }

        @Override
        public List<String> getName(GuiBase gui) {
            if (item != null && item.getItem() != null) {
                return item.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
            }else{
                List<String> ret = new ArrayList<String>();
                ret.add("Unknown");
                return ret;
            }
        }

        @Override
        public int getAmount() {
            return item == null ? 0 : item.stackSize;
        }

        @Override
        public void setAmount(int val) {
            if (item != null) {
                item.stackSize = val;
            }
        }

        @Override
        public Element copy() {
            return new ElementItem(item == null ? null : item.copy());
        }
    }

    public static class ElementFluid extends Element<Fluid> {
        private int size;
        private ElementFluid(Fluid fluid) {
            this.item = fluid;
        }

        @Override
        public void draw(GuiBase gui, int x, int y, int mX, int mY) {
            gui.drawFluid(item, x, y, mX, mY);
        }

        @Override
        public List<String> getName(GuiBase gui) {
            List<String> ret = new ArrayList<String>();
            ret.add(item.getLocalizedName());
            return ret;
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
        public Element copy() {
            ElementFluid ret = new ElementFluid(item == null ? null : item);
            ret.size = size;
            return ret;
        }
    }

    private static final int ARROW_X_LEFT = 20;
    private static final int ARROW_X_RIGHT = 150;
    private static final int ARROW_Y = 40;
    private static final int ARROW_SRC_X = 244;
    private static final int ARROW_SRC_Y = 176;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 10;

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

    private int id;
    private Type type;
    protected Element selected;
    private List<Element> playerItems;
    private List<Element> searchItems;
    private ItemPrecision precision;
    private boolean clicked;


    private Element getSelected() {
        return selected;
    }
    public GuiEditMenuItem(GuiBase gui, EntityPlayer player, Object obj, int id, Type type, int amount, ItemPrecision precision) {
        this(gui, player, obj instanceof ItemStack ? new ElementItem((ItemStack)obj) : new ElementFluid((Fluid)obj), id, type, amount, precision) ;
    }

    public GuiEditMenuItem(GuiBase gui, EntityPlayer player, Element element, int id, final Type type, final int amount, ItemPrecision precision) {
        super(gui, player, true);
        this.selected = element;
        this.id = id;
        this.type = type;
        this.precision = precision;

        playerItems = new ArrayList<Element>();
        searchItems = new ArrayList<Element>();
        IInventory inventory = Minecraft.getMinecraft().thePlayer.inventory;
        int itemLength = inventory.getSizeInventory();
        for (int i = 0; i < itemLength; i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != null) {
                item = item.copy();
                item.stackSize = 1;
                boolean exists = false;
                for (Element other : playerItems) {
                    if (ItemStack.areItemStacksEqual(item, (ItemStack)other.getItem())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists && item.getItem() != ModItems.book) {
                    playerItems.add(new ElementItem(item));
                }
            }
        }
        if (type.allowFluids) {
            List<Integer> fluids = new ArrayList<Integer>();
            int end = playerItems.size();
            for (int i = 0; i < end; i++) {
                Element item = playerItems.get(i);
                FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem((ItemStack) item.getItem());
                if (fluidStack != null && !fluids.contains(fluidStack.fluidID)) {
                    fluids.add(fluidStack.fluidID);
                    playerItems.add(new ElementFluid(fluidStack.getFluid()));
                    if (playerItems.size() == PLAYER_LINES * ITEMS_PER_LINE) {
                        break;
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
                protected void textChanged(GuiBase gui) {
                    try{
                        int number;
                        if (getText().equals("")) {
                            number = 1;
                        }else{
                            number = Integer.parseInt(getText());
                        }

                        if (number == 0) {
                            number = 1;
                        }

                        if (getSelected() != null) {
                            getSelected().setAmount(number);
                        }
                    }catch (Exception ignored) {}

                }
            });
        }
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 230, 18, false) {
            @Override
            protected void textChanged(GuiBase gui) {
                searchItems.clear();
                if (!getText().equals("")) {
                    String search = getText().toLowerCase();

                    List<ItemStack> itemStacks = new ArrayList<ItemStack>();
                    Iterator itemTypeIterator = Item.itemRegistry.iterator();
                    while (itemTypeIterator.hasNext()){
                        Item item = (Item)itemTypeIterator.next();

                        if (item != null && item.getCreativeTab() != null) {
                            item.getSubItems(item, null, itemStacks);
                        }
                    }

                    for (ItemStack itemStack : itemStacks) {
                        if (itemStack != null) {
                            searchItems.add(new ElementItem(itemStack));
                        }
                    }

                    if (type.allowFluids) {
                        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
                            searchItems.add(new ElementFluid(fluid));
                        }
                    }

                    Iterator<Element> itemIterator = searchItems.iterator();
                    int kept = 0;
                    while (itemIterator.hasNext()) {
                        Element element = itemIterator.next();
                        if (kept >= SEARCH_LINES * ITEMS_PER_LINE) {
                            itemIterator.remove();
                            continue;
                        }

                        List<String> description;

                        //if it encounters some weird items
                        try {
                            description = element.getName(gui);
                        }catch (Throwable ex) {
                            itemIterator.remove();
                            continue;
                        }

                        Iterator<String> descriptionIterator = description.iterator();

                        boolean foundSequence = false;

                        while (descriptionIterator.hasNext()) {
                            String line = descriptionIterator.next().toLowerCase();
                            if (line.contains(search)) {
                                foundSequence = true;
                                break;
                            }
                        }

                        if (!foundSequence) {
                            itemIterator.remove();
                        }else{
                            kept++;
                        }
                    }

                }
            }
        });
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
            GL11.glColor3f(1F, 1F, 1F);

                ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            drawArrow(gui, mX, mY, true);
            drawArrow(gui, mX, mY, false);
            gui.drawCenteredString(precision.toString(), ARROW_X_LEFT + ARROW_W, ARROW_Y, 0.7F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
        }

    }


    @Override
    public void drawMouseOver(GuiBase gui, int mX, int mY) {
        super.drawMouseOver(gui, mX, mY);

        drawListMouseOver(gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);
        drawListMouseOver(gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);
    }

    private void drawList(GuiBase gui, int x, int y, List<Element> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;

            element.draw(gui, x + xI * OFFSET, y + yI * OFFSET, mX, mY);
        }
    }

    private void drawListMouseOver(GuiBase gui, int x, int y, List<Element> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element element = items.get(i);
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

    private void clickList(GuiBase gui, int x, int y, List<Element> items, int mX, int mY) {
        for (int i = 0; i < items.size(); i++) {
            Element element = items.get(i);
            int xI = i % ITEMS_PER_LINE;
            int yI = i / ITEMS_PER_LINE;

            if (gui.inBounds(x + xI * OFFSET, y + yI * OFFSET, SIZE, SIZE, mX, mY)) {
                if (element != null) {
                    selected = element.copy();
                    if (amountTextBox != null) {
                        amountTextBox.textChanged(gui);
                    }else{
                        selected.setAmount(1);
                    }
                }
                break;
            }
        }

    }

    private static final int PLAYER_X = 20;
    private static final int PLAYER_Y = 80;
    private static final int SEARCH_X = 180;
    private static final int SEARCH_Y = 30;
    private static final int SIZE = 18;
    private static final int OFFSET = 20;
    private static final int ITEMS_PER_LINE = 7;
    private static final int SEARCH_LINES = 9;
    private static final int PLAYER_LINES = 6;

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);


        clickList(gui, PLAYER_X, PLAYER_Y, playerItems, mX, mY);
        clickList(gui, SEARCH_X, SEARCH_Y, searchItems, mX, mY);

        textBoxes.onClick(gui, mX, mY);

        if (usePrecision()) {
            if (inArrowBounds(gui, mX, mY, true)) {
                precision = ItemPrecision.values()[(precision.ordinal() + ItemPrecision.values().length - 1) % ItemPrecision.values().length];
                clicked = true;
            }else if (inArrowBounds(gui, mX, mY, false)) {
                precision = ItemPrecision.values()[(precision.ordinal() + 1) % ItemPrecision.values().length];
                clicked = true;
            }
        }
    }

    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);
        clicked = false;
    }

    @Override
    public void onKeyTyped(GuiBase gui, char c, int k) {
        super.onKeyTyped(gui, c, k);

        textBoxes.onKeyStroke(gui, c, k);
    }

    @Override
    protected void save(GuiBase gui) {
        if (type == Type.BAG_ITEM) {
            if (GuiQuestBook.getSelectedGroup() != null && selected instanceof ElementItem && (ItemStack)selected.getItem() != null) {
                GuiQuestBook.getSelectedGroup().setItem(id, (ItemStack)selected.getItem());
            }
        }else if (type == Type.QUEST_ICON) {
            if (Quest.getQuest(id)  != null && selected instanceof ElementItem) {
                try {
                    Quest.getQuest(id).setIcon((ItemStack) selected.getItem());
                }catch(Exception e){
                    System.out.println("Tell LordDusk that he found the issue.");
                }
                SaveHelper.add(SaveHelper.EditType.ICON_CHANGE);
            }
        }else{
            ((GuiQuestBook)gui).getSelectedQuest().setItem(selected, id, type, precision, player);
        }
    }




    private TextBoxGroup.TextBox amountTextBox;
    private TextBoxGroup textBoxes;

}
