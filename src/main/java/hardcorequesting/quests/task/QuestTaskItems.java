package hardcorequesting.quests.task;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.integration.jei.JEIIntegration;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.UUID;

public abstract class QuestTaskItems extends QuestTask {

    private static final int MAX_X = 300;
    private static final int OFFSET = 20;
    private static final int SIZE = 18;
    private static final int TEXT_HEIGHT = 9;
    private int lastClicked;
    ItemRequirement[] items;

    public QuestTaskItems(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        setItems(new ItemRequirement[0]);
    }

    public ItemRequirement[] getItems() {
        return items;
    }

    public void setItems(ItemRequirement[] items) {
        this.items = items;
        setPositions(this.items);
    }

    public void setItem(GuiEditMenuItem.Element element, int id, ItemPrecision precision) {
        if (element.getFluidStack() == null) return;

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
                ItemStack stack = item.getFluidStack().copy();
                stack.setCount(1);
                items[id].stack = stack;
            } else {
                GuiEditMenuItem.ElementFluid fluid = (GuiEditMenuItem.ElementFluid) element;
                items[id].hasItem = false;
                items[id].fluid = fluid.getFluidStack();
                items[id].stack = null;
            }
            items[id].required = element.getAmount();
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

    protected void resetTask(UUID playerId, int id) {
        getData(playerId).completed = false;
        ((QuestDataTaskItems) getData(playerId)).progress[id] = 0;
    }

    protected void completeTask(UUID playerId, int id, int count) {
        QuestDataTaskItems data = (QuestDataTaskItems) getData(playerId);
        data.progress[id] = count;
        doCompletionCheck(data, playerId);
    }

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

    @SideOnly(Side.CLIENT)
    private ItemRequirement[] getEditFriendlyItems(ItemRequirement[] items) {
        if (Quest.canQuestsBeEdited()) {
            items = Arrays.copyOf(items, items.length + 1);
        } else {
            return items;
        }

        items[items.length - 1] = new ItemRequirement(ItemStack.EMPTY, 1);
        setPositions(items);
        return items;
    }

    @SideOnly(Side.CLIENT)
    protected abstract GuiEditMenuItem.Type getMenuTypeId();

    public boolean increaseItems(NonNullList<ItemStack> itemsToConsume, QuestDataTaskItems data, UUID playerId) {
        if (!parent.isAvailable(playerId)) return false;

        boolean updated = false;

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if(!item.hasItem || data.isDone(i, item)){
                continue;
            }
    
            for(int j = 0; j < itemsToConsume.size(); j++){
                ItemStack stack = itemsToConsume.get(j);
                if(item.precision.areItemsSame(stack, item.stack)){
                    int amount = Math.min(stack.getCount(), item.required - data.progress[i]);
                    if(amount > 0){
                        stack.shrink(amount);
                        if(stack.getCount() == 0){
                            itemsToConsume.set(j, ItemStack.EMPTY);
                        }
                        data.progress[i] += amount;
                        updated = true;
                    }
                }
            }
        }

        if (updated) {
            doCompletionCheck(data, playerId);
        }

        return updated;
    }

    protected void doCompletionCheck(QuestDataTaskItems data, UUID playerId) {
        boolean isDone = true;
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (!data.isDone(i, item)) {
                isDone = false;
                break;
            }
        }

        if (isDone) {
            completeTask(playerId);
        }
        parent.sendUpdatedDataToTeam(playerId);
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskItems.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        ItemRequirement[] items = getEditFriendlyItems(this.items);

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.hasItem) {
                gui.drawItemStack(item.getPermutatedItem(), item.x, item.y, mX, mY, false);
            } else {
                gui.drawFluid(item.fluid, item.x, item.y, mX, mY);
            }

            String str = (getProgress(player, i) * 100 / item.required) + "%";
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 200);// magic z value to write over fluidStack render
            float textSize = 0.8F;
            gui.drawStringWithShadow(str, (int) (item.x + SIZE - gui.getStringWidth(str) * textSize), (int) (item.y + SIZE - TEXT_HEIGHT * textSize + 2), textSize, getProgress(player, i) == item.required ? 0x308030 : 0xFFFFFF);
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                GuiQuestBook.setSelectedStack(item.getStack());
                String str = "";
                if (getProgress(player, i) == item.required) {
                    str += GuiColor.GREEN;
                }
                str += item.getDisplayName() + ": " + getProgress(player, i) + "/" + item.required;
                if (Quest.canQuestsBeEdited())
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

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        boolean isOpBookWithShiftKeyDown = gui.isOpBook && GuiScreen.isShiftKeyDown();
        boolean doubleClick = false;
        if (Quest.canQuestsBeEdited() || isOpBookWithShiftKeyDown) {

            ItemRequirement[] items = getEditFriendlyItems(this.items);

            for (int i = 0; i < items.length; i++) {
                ItemRequirement item = items[i];
                if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                    int lastDiff = player.ticksExisted - lastClicked;
                    if (lastDiff < 6) {
                        doubleClick = true;
                    } else {
                        lastClicked = player.ticksExisted;
                    }

                    if (isOpBookWithShiftKeyDown) {
                        if (getProgress(player, i) == item.required) {
                            resetTask(player.getPersistentID(), i);
                        } else {
                            completeTask(player.getPersistentID(), i, item.required);
                        }
                    } else if (Quest.canQuestsBeEdited()) {
                        if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, item.hasItem ? item.stack != null ? item.stack.copy() : null : item.fluid, i, getMenuTypeId(), item.required, item.precision));
                        } else if (gui.getCurrentMode() == EditMode.DELETE && ((item.stack != null && !item.stack.isEmpty()) || item.fluid != null)) {
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
                    }
                    break;
                }
            }
        } else {
            if (Loader.isModLoaded("jei")) {
                for (ItemRequirement item : getEditFriendlyItems(this.items)) {
                    if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                        JEIIntegration.showItemStack(item.getStack());
                        return;
                    }
                }
            }
        }
    }

    @Override
    public float getCompletedRatio(UUID playerId) {
        QuestDataTaskItems data = (QuestDataTaskItems) getData(playerId);
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
    public void mergeProgress(UUID playerId, QuestDataTask own, QuestDataTask other) {
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
            completeTask(playerId);
        }
    }

    @Override
    public void autoComplete(UUID playerId, boolean status) {
        QuestDataTaskItems data = (QuestDataTaskItems) getData(playerId);
        for (int i = 0; i < items.length; i++) {
            if (status) {
                data.progress[i] = items[i].required;
            } else {
                data.progress[i] = 0;
            }
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);

        int[] ownProgress = ((QuestDataTaskItems) own).progress;
        int[] otherProgress = ((QuestDataTaskItems) other).progress;


        System.arraycopy(otherProgress, 0, ownProgress, 0, ownProgress.length);
    }

    public static class ItemRequirement {

        private static int CYCLE_TIME = 2;//2 second cycle
        public Fluid fluid;
        public int required;
        public boolean hasItem;
        private ItemStack stack = ItemStack.EMPTY;
        private ItemPrecision precision = ItemPrecision.PRECISE;
        private ItemStack[] permutations;
        private int cycleAt = -1;
        private int current = 0;
        private int last;
        private int x;
        private int y;

        public ItemRequirement(ItemStack stack, int required) {
            this.stack = stack;
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

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack;
            this.permutations = null;
        }

        private void setPermutations() {
            if (stack == null) return;
            permutations = precision.getPermutations(stack);
            if (permutations != null && permutations.length > 0) {
                last = permutations.length - 1;
                cycleAt = -1;
            }
        }

        public ItemStack getPermutatedItem() {
            if (permutations == null && precision.hasPermutations())
                setPermutations();
            if (permutations == null || permutations.length < 2)
                return stack != null ? stack : ItemStack.EMPTY;
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

        public String getDisplayName() {
            ItemStack stack = getPermutatedItem();
            if(hasItem){
                if(!stack.isEmpty()){
                    return stack.getDisplayName();
                } else {
                    return "Nothing";
                }
            } else {
                return fluid.getLocalizedName(null);
            }
        }
    }
}
