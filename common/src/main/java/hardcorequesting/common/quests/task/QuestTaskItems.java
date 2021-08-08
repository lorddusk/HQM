package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class QuestTaskItems extends QuestTask {
    
    private static final String ITEMS = "items";
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
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (ItemRequirement item : getItems()) {
            array.add(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER.toJsonTree(item));
        }
        builder.add(ITEMS, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        List<ItemRequirement> list = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, ITEMS, new JsonArray())) {
            ItemRequirement requirement = QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER.fromJsonTree(element);
            if (requirement != null)
                list.add(requirement);
        }
        setItems(list.toArray(new ItemRequirement[0]));
    }
    
    public ItemRequirement[] getItems() {
        return items;
    }
    
    public void setItems(ItemRequirement[] items) {
        this.items = items;
        setPositions(this.items);
    }
    
    @Environment(EnvType.CLIENT)
    public void setItem(GuiEditMenuItem.Result<?> result, int id) {
        if (result.isEmpty()) return;
        
        if (id >= items.length) {
            this.items = getEditFriendlyItems(items);
            SaveHelper.add(SaveHelper.EditType.TASK_ITEM_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.TASK_ITEM_CHANGE);
        }
        
        if (id < items.length) {
            if (result.get() instanceof ItemStack) {
                items[id].hasItem = true;
                items[id].fluid = null;
                items[id].stack = ((ItemStack) result.get()).copy();
            } else {
                items[id].hasItem = false;
                items[id].fluid = ((FluidStack) result.get());
                items[id].stack = null;
            }
            items[id].required = result.getAmount();
            items[id].precision = result.getPrecision();
            items[id].permutations = null;
        }
    }
    
    private int getProgress(Player player, int id) {
        if (id >= items.length) {
            return 0;
        }
        
        QuestDataTaskItems data = ((QuestDataTaskItems) getData(player));
        if (id >= data.progress.length) {
            data.progress = Arrays.copyOf(data.progress, data.progress.length + 1);
        }
        
        return data.progress[id];
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
    
    @Environment(EnvType.CLIENT)
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
    
    @Environment(EnvType.CLIENT)
    protected abstract GuiEditMenuItem.Type getMenuTypeId();
    
    public boolean increaseItems(NonNullList<ItemStack> itemsToConsume, QuestDataTaskItems data, UUID playerId) {
        if (!parent.isAvailable(playerId)) return false;
        
        boolean updated = false;
        
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (!item.hasItem || data.isDone(i, item)) {
                continue;
            }
            
            for (int j = 0; j < itemsToConsume.size(); j++) {
                ItemStack stack = itemsToConsume.get(j);
                if (item.precision.areItemsSame(stack, item.stack)) {
                    int amount = Math.min(stack.getCount(), item.required - data.progress[i]);
                    if (amount > 0) {
                        stack.shrink(amount);
                        if (stack.getCount() == 0) {
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
    
    public void doCompletionCheck(QuestDataTaskItems data, UUID playerId) {
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
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        ItemRequirement[] items = getEditFriendlyItems(this.items);
        
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.hasItem) {
                gui.drawItemStack(item.getPermutatedItem(), item.x, item.y, mX, mY, false);
            } else if (item.fluid != null) {
                gui.drawFluid(item.fluid, matrices, item.x, item.y, mX, mY);
            }
            
            FormattedText str = Translator.plain((getProgress(player, i) * 100 / item.required) + "%");
            matrices.pushPose();
            matrices.translate(0, 0, 200);// magic z value to write over stack render
            float textSize = 0.8F;
            gui.drawStringWithShadow(matrices, str, (int) (item.x + SIZE - gui.getStringWidth(str) * textSize), (int) (item.y + SIZE - (item.hasItem && !item.stack.isEmpty() && item.stack.getCount() != 1 ? TEXT_HEIGHT : 0) - TEXT_HEIGHT * textSize + 2), textSize, getProgress(player, i) == item.required ? 0x308030 : 0xFFFFFF);
            matrices.popPose();
        }
        
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                GuiQuestBook.setSelectedStack(item.getStack());
                ItemStack stack = item.getStack();
                List<FormattedText> str = new ArrayList<>();
                if (item.fluid != null) {
                    List<Component> list = new ArrayList<>();
                    str.add(new TextComponent(item.fluid.getName().getString()));
                    if (Minecraft.getInstance().options.advancedItemTooltips) {
                        String entryId = Registry.FLUID.getKey(item.fluid.getFluid()).toString();
                        list.add(new TextComponent(entryId).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    str.addAll(list);
                } else if (stack != null && !stack.isEmpty()) {
                    str.addAll(gui.getTooltipFromItem(stack));
                }
                
                str.add(FormattedText.composite(Translator.translatable("hqm.questBook.itemRequirementProgress"), Translator.plain(": " + getProgress(player, i) + "/" + item.required)));
                if (item.fluid == null && Quest.canQuestsBeEdited()) {
                    str.add(FormattedText.EMPTY);
                    str.add(Translator.text(item.getPrecision().getName(), GuiColor.GRAY));
                }
                if (gui.isOpBook && Screen.hasShiftDown()) {
                    if (getProgress(player, i) == item.required) {
                        str.addAll(Arrays.asList(FormattedText.EMPTY, FormattedText.EMPTY, Translator.translatable("hqm.questBook.resetTask", GuiColor.RED)));
                    } else {
                        str.addAll(Arrays.asList(FormattedText.EMPTY, FormattedText.EMPTY, Translator.translatable("hqm.questBook.completeTask", GuiColor.ORANGE)));
                    }
                }
                gui.renderTooltipL(matrices, str, mX + gui.getLeft(), mY + gui.getTop());
                break;
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        boolean isOpBookWithShiftKeyDown = gui.isOpBook && Screen.hasShiftDown();
        boolean doubleClick = false;
        if (Quest.canQuestsBeEdited() || isOpBookWithShiftKeyDown) {
            ItemRequirement[] items = getEditFriendlyItems(this.items);
            
            for (int i = 0; i < items.length; i++) {
                ItemRequirement item = items[i];
                if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                    int lastDiff = player.tickCount - lastClicked;
                    if (lastDiff < 0) {
                        lastClicked = player.tickCount;
                    } else if (lastDiff < 6) {
                        doubleClick = true;
                    } else {
                        lastClicked = player.tickCount;
                    }
                    
                    if (isOpBookWithShiftKeyDown) {
                        OPBookHelper.reverseRequirementCompletion(this, i, player);
                    } else if (Quest.canQuestsBeEdited()) {
                        if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
                            final int id = i;
                            GuiEditMenuItem.display(gui, player, item.hasItem ? item.stack != null ? item.stack.copy() : null : item.fluid, getMenuTypeId(), item.required, item.precision,
                                    result -> this.setItem(result, id));
                            
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
            // TODO REI
//            if (Loader.isModLoaded("jei")) {
//                for (ItemRequirement item : getEditFriendlyItems(this.items)) {
//                    if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
//                        JEIIntegration.showItemStack(item.getStack());
//                        return;
//                    }
//                }
//            }
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
        public FluidStack fluid;
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
        
        public ItemRequirement(FluidStack fluid, int required) {
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
    }
}
