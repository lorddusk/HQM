package hardcorequesting.quests;


import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.*;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class QuestTaskItems extends QuestTask {

    public QuestTaskItems(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        setItems(new ItemRequirement[0]);
    }

    public void setItems(ItemRequirement[] items) {
        this.items = items;
        setPositions(this.items);
    }

    public ItemRequirement[] getItems() {
        return items;
    }


    public static class ItemRequirement {
        private ItemStack item;
        public Fluid fluid;
        public int required;
        public boolean hasItem;
        private ItemPrecision precision = ItemPrecision.PRECISE;

        public ItemRequirement(ItemStack item, int required) {
            this.item = item;
            this.required = required;
            this.hasItem = true;
        }

        public ItemRequirement(Fluid fluid, int required) {
            this.fluid = fluid;
            this.required = required;
            this.hasItem = false;
        }

        public ItemPrecision getPrecision() {
            return precision;
        }

        public void setPrecision(ItemPrecision precision) {
            this.precision = precision;
            permutations = null;
        }

        public ItemStack getItem() {
            return item;
        }

        public void setItem(ItemStack item) {
            this.item = item;
            this.permutations = null;
        }

        private ItemStack[] permutations;
        private int cycleAt = -1;
        private int current = 0;
        private int last;
        private static int CYCLE_TIME = 2;//2 second cycle

        private void setPermutations() {
            if (item == null) return;
            permutations = precision.getPermutations(item);
            if (permutations != null && permutations.length > 0) {
                last = permutations.length - 1;
                cycleAt = -1;
            }
        }

        public ItemStack getPermutatedItem() {
            if (permutations == null && precision.hasPermutations())
                setPermutations();
            if (permutations == null || permutations.length < 2)
                return item;
            int ticks = (int) (System.currentTimeMillis() / 1000);
            if (cycleAt == -1)
                cycleAt = ticks + CYCLE_TIME;
            if (ticks >= cycleAt) {
                if (++current > last) current = 0;
                while (ticks >= cycleAt)
                    cycleAt += CYCLE_TIME;
            }
            return permutations[current];
        }

        private int x;
        private int y;

        public String getDisplayName() {
            ItemStack item = getPermutatedItem();
            if (hasItem && item == null) {
                return "Nothing";
            } else if (item != null) {
                return item.getItem() != null ? item.getDisplayName() : "Unknown";
            } else {
                return fluid.getLocalizedName(null);
            }
        }
    }

    public void setItem(GuiEditMenuItem.Element element, int id, ItemPrecision precision) {
        if (element.getItem() == null) return;

        if (id >= items.length) {
            this.items = getEditFriendlyItems(items);
            SaveHelper.add(SaveHelper.EditType.TASK_ITEM_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.TASK_ITEM_CHANGE);
        }

        if (id < items.length) {
            if (element instanceof GuiEditMenuItem.ElementItem) {
                GuiEditMenuItem.ElementItem item = (GuiEditMenuItem.ElementItem) element;
                items[id].hasItem = true;
                items[id].fluid = null;
                ItemStack itemStack = item.getItem().copy();
                itemStack.stackSize = 1;
                items[id].item = itemStack;
            } else {
                GuiEditMenuItem.ElementFluid fluid = (GuiEditMenuItem.ElementFluid) element;
                items[id].hasItem = false;
                items[id].fluid = fluid.getItem();
                items[id].item = null;
            }
            items[id].required = Math.min(DataBitHelper.ITEM_PROGRESS.getMaximum(), element.getAmount());
            items[id].precision = precision;
            items[id].permutations = null;
        }
    }

    private int getProgress(EntityPlayer player, int id) {
        if (id >= items.length) {
            return 0;
        }

        QuestDataTaskItems data = ((QuestDataTaskItems) getData(player));
        if (id >= data.progress.length) {
            data.progress = Arrays.copyOf(data.progress, data.progress.length + 1);
        }

        return data.progress[id];
    }

    protected void resetTask(String playerName, int id) {
        getData(playerName).completed = false;
        ((QuestDataTaskItems) getData(playerName)).progress[id] = 0;
    }

    protected void completeTask(String playerName, int id, int count) {
        QuestDataTaskItems data = (QuestDataTaskItems) getData(playerName);
        data.progress[id] = count;
        doCompletionCheck(data, playerName);
    }


    ItemRequirement[] items;


    private void setPositions(ItemRequirement[] items) {
        int x = START_X;
        int y = START_Y;

        for (ItemRequirement item : items) {
            item.x = x;
            item.y = y;

            x += OFFSET;
            if (x > MAX_X) {
                x = START_X;
                y += OFFSET;
            }
        }
    }

    @Override
    public void read(DataReader dr, QuestDataTask task, FileVersion version, boolean light) {
        super.read(dr, task, version, light);
        if (light) {
            for (int i = 0; i < items.length; i++) {
                ((QuestDataTaskItems) task).progress[i] = dr.readData(DataBitHelper.ITEM_PROGRESS);
            }
        } else {
            int count = dr.readData(DataBitHelper.TASK_ITEM_COUNT);
            for (int i = 0; i < count; i++) {
                int progress = dr.readData(DataBitHelper.ITEM_PROGRESS);
                if (i < ((QuestDataTaskItems) task).progress.length) {
                    ((QuestDataTaskItems) task).progress[i] = Math.min(items[i].required, Math.max(0, progress));
                }
            }
        }
    }

    @Override
    public void write(DataWriter dw, QuestDataTask task, boolean light) {
        super.write(dw, task, light);

        if (!light) {
            dw.writeData(items.length, DataBitHelper.TASK_ITEM_COUNT);
        }
        for (int i = 0; i < items.length; i++) {
            dw.writeData(((QuestDataTaskItems) task).progress[i], DataBitHelper.ITEM_PROGRESS);
        }

    }

    @Override
    public void save(DataWriter dw) {
        dw.writeData(items.length, DataBitHelper.TASK_ITEM_COUNT);
        for (ItemRequirement item : items) {
            dw.writeBoolean(item.hasItem);
            if (item.hasItem) {
                dw.writeItem(item.item.getItem());
                dw.writeData(item.item.getItemDamage(), DataBitHelper.SHORT);
                dw.writeNBT(item.item.getTagCompound());
                dw.writeData(item.required, DataBitHelper.TASK_REQUIREMENT);
                dw.writeString(ItemPrecision.getUniqueID(item.precision), DataBitHelper.ITEM_PRECISION);
            } else {
                FluidStack fluidStack = new FluidStack(item.fluid, item.required);
                NBTTagCompound compound = new NBTTagCompound();
                fluidStack.writeToNBT(compound);
                dw.writeNBT(compound);
            }
        }
    }

    @Override
    public void load(DataReader dr, FileVersion version) {
        int count = dr.readData(DataBitHelper.TASK_ITEM_COUNT);
        ItemRequirement[] items = new ItemRequirement[count];
        for (int i = 0; i < items.length; i++) {
            boolean isItem = dr.readBoolean();
            if (isItem) {
                Item item = dr.readItem();

                if (Quest.isEditing) {
                    if (item == null) {
                        FMLLog.log("HQM-EDIT", Level.INFO, "Changed invalid item to bedrock.");
                        item = Item.getItemFromBlock(Blocks.bedrock);
                    }
                }

                int dmg = dr.readData(DataBitHelper.SHORT);
                NBTTagCompound compound = dr.readNBT();
                ItemStack itemStack = new ItemStack(item, 1, dmg);
                itemStack.setTagCompound(compound);
                items[i] = new ItemRequirement(itemStack, dr.readData(DataBitHelper.TASK_REQUIREMENT));
                items[i].precision = version.lacks(FileVersion.CUSTOM_PRECISION_TYPES) ?
                        ItemPrecision.getOldPrecisionType(dr.readData(DataBitHelper.ITEM_PRECISION))
                        : ItemPrecision.getPrecisionType(dr.readString(DataBitHelper.ITEM_PRECISION));
            } else {
                NBTTagCompound compound = dr.readNBT();
                FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(compound);
                if (fluidStack != null) {
                    items[i] = new ItemRequirement(fluidStack.getFluid(), fluidStack.amount);
                }
            }
        }

        setItems(items);
    }

    private static final int MAX_X = 300;
    private static final int OFFSET = 20;
    private static final int SIZE = 18;
    private static final int TEXT_HEIGHT = 9;

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        ItemRequirement[] items = getEditFriendlyItems(this.items);

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.hasItem) {
                gui.drawItem(item.getPermutatedItem(), item.x, item.y, mX, mY, false);
            } else {
                //Todo fix fluid drawing
                //gui.drawFluid(item.fluid, item.x, item.y, mX, mY);
            }

            String str = (getProgress(player, i) * 100 / item.required) + "%";
            //float z = gui.getZLevel();
            //gui.setZLevel(z + 100);
            GL11.glPushMatrix();
            GL11.glTranslatef(0, 0, 100);
            float textSize = 0.8F;
            gui.drawStringWithShadow(str, (int) (item.x + SIZE - gui.getStringWidth(str) * textSize), (int) (item.y + SIZE - TEXT_HEIGHT * textSize + 2), textSize, getProgress(player, i) == item.required ? 0x308030 : 0xFFFFFF);
            GL11.glPopMatrix();
            //gui.setZLevel(z);
        }

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                GuiQuestBook.setSelected(item.getItem());
                String str = "";
                if (getProgress(player, i) == item.required) {
                    str += GuiColor.GREEN;
                }
                str += item.getDisplayName() + ": " + getProgress(player, i) + "/" + item.required;
                if (Quest.isEditing)
                    str += "\n" + GuiColor.GRAY + item.getPrecision().getName();
                if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
                    if (getProgress(player, i) == item.required) {
                        str += "\n\n" + GuiColor.RED + Translator.translate("hqm.questBook.resetTask");
                    } else {
                        str += "\n\n" + GuiColor.ORANGE + Translator.translate("hqm.questBook.completeTask");
                    }
                }
                gui.drawMouseOver(str, mX + gui.getLeft(), mY + gui.getTop());
                break;
            }
        }
    }

    private ItemRequirement[] getEditFriendlyItems(ItemRequirement[] items) {
        if (Quest.isEditing && items.length < DataBitHelper.TASK_ITEM_COUNT.getMaximum()) {
            items = Arrays.copyOf(items, items.length + 1);
        } else {
            return items;
        }

        items[items.length - 1] = new ItemRequirement((ItemStack) null, 1);
        setPositions(items);
        return items;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        boolean isOpBookWithShiftKeyDown = gui.isOpBook && GuiScreen.isShiftKeyDown();
        if (Quest.isEditing || isOpBookWithShiftKeyDown) {

            ItemRequirement[] items = getEditFriendlyItems(this.items);

            for (int i = 0; i < items.length; i++) {
                ItemRequirement item = items[i];
                if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                    if (isOpBookWithShiftKeyDown) {
                        if (getProgress(player, i) == item.required) {
                            resetTask(QuestingData.getUserName(player), i);
                        } else {
                            completeTask(QuestingData.getUserName(player), i, item.required);
                        }
                    } else if (Quest.isEditing && gui.getCurrentMode() == EditMode.ITEM) {
                        gui.setEditMenu(new GuiEditMenuItem(gui, player, item.hasItem ? item.item != null ? item.item.copy() : null : item.fluid, i, getMenuTypeId(), item.required, item.precision));
                    } else if (Quest.isEditing && gui.getCurrentMode() == EditMode.DELETE && (item.item != null || item.fluid != null)) {
                        ItemRequirement[] newItems = new ItemRequirement[this.items.length - 1];
                        int id = 0;
                        for (int j = 0; j < this.items.length; j++) {
                            if (j != i) {
                                newItems[id] = this.items[j];
                                id++;
                            }
                        }
                        setItems(newItems);
                        SaveHelper.add(SaveHelper.EditType.TASK_ITEM_REMOVE);
                    }
                    break;
                }
            }

        }
    }

    @SideOnly(Side.CLIENT)
    protected abstract GuiEditMenuItem.Type getMenuTypeId();


    public boolean increaseItems(ItemStack[] itemsToConsume, QuestDataTaskItems data, String playerName) {
        if (!parent.isAvailable(playerName)) return false;


        boolean updated = false;

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (!item.hasItem || item.required == data.progress[i]) {
                continue;
            }

            for (int j = 0; j < itemsToConsume.length; j++) {
                ItemStack itemStack = itemsToConsume[j];
                if (item.precision.areItemsSame(itemStack, item.item)) {
                    int amount = Math.min(itemStack.stackSize, item.required - data.progress[i]);
                    if (amount > 0) {
                        itemStack.stackSize -= amount;
                        if (itemStack.stackSize == 0) {
                            itemsToConsume[j] = null;
                        }
                        data.progress[i] += amount;
                        updated = true;
                    }
                }
            }
        }


        if (updated) {
            doCompletionCheck(data, playerName);
        }

        return updated;
    }

    protected void doCompletionCheck(QuestDataTaskItems data, String playerName) {
        boolean isDone = true;
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.required > data.progress[i]) {
                isDone = false;
                break;
            }
        }

        if (isDone) {
            completeTask(playerName);
        }
        parent.sendUpdatedDataToTeam(playerName);
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskItems.class;
    }

    @Override
    public float getCompletedRatio(String playerName) {
        QuestDataTaskItems data = (QuestDataTaskItems) getData(playerName);
        int done = 0;
        int total = 0;
        for (int count : data.progress) {
            done += count;
        }
        for (ItemRequirement item : items) {
            total += item.required;
        }

        return Math.max(0, Math.min(1, done / (float) total));
    }

    @Override
    public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
        int[] ownProgress = ((QuestDataTaskItems) own).progress;
        int[] otherProgress = ((QuestDataTaskItems) other).progress;

        boolean completed = true;
        for (int i = 0; i < ownProgress.length; i++) {
            ownProgress[i] = Math.max(ownProgress[i], otherProgress[i]);
            if (ownProgress[i] != items[i].required) {
                completed = false;
            }
        }

        if (completed) {
            completeTask(playerName);
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);

        int[] ownProgress = ((QuestDataTaskItems) own).progress;
        int[] otherProgress = ((QuestDataTaskItems) other).progress;


        System.arraycopy(otherProgress, 0, ownProgress, 0, ownProgress.length);
    }

    @Override
    public void autoComplete(String playerName) {
        QuestDataTaskItems data = (QuestDataTaskItems) getData(playerName);
        for (int i = 0; i < items.length; i++) {
            data.progress[i] = items[i].required;
        }
    }
}
