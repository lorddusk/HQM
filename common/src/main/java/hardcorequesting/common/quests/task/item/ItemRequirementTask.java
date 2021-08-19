package hardcorequesting.common.quests.task.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import hardcorequesting.common.quests.task.ListTask;
import hardcorequesting.common.util.*;
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

public abstract class ItemRequirementTask extends ListTask<ItemRequirementTask.Part, QuestDataTaskItems> {
    
    private static final String ITEMS = "items";
    private static final int MAX_X = 300;
    private static final int OFFSET = 20;
    private static final int SIZE = 18;
    private static final int TEXT_HEIGHT = 9;
    private int lastClicked;
    
    public ItemRequirementTask(Quest parent, String description, String longDescription) {
        super(EditType.Type.TASK_ITEM, parent, description, longDescription);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part(ItemStack.EMPTY, 1);
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(ITEMS, writeElements(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        readElements(GsonHelper.getAsJsonArray(object, ITEMS, new JsonArray()), QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER);
    }
    
    public List<Part> getItems() {
        return elements;
    }
    
    @Environment(EnvType.CLIENT)
    public void setItem(Either<ItemStack, FluidStack> item, int amount, ItemPrecision precision, int id) {
        
        Part requirement = getOrCreateForModify(id);
    
        item.ifLeft(itemStack -> {
            requirement.hasItem = true;
            requirement.fluid = null;
            requirement.stack = itemStack;
        }).ifRight(fluidStack -> {
            requirement.hasItem = false;
            requirement.fluid = fluidStack;
            requirement.stack = null;
        });
        requirement.required = amount;
        requirement.precision = precision;
        requirement.permutations = null;
    }
    
    private int getProgress(Player player, int id) {
        if (id >= elements.size()) {
            return 0;
        }
        
        return getData(player).getValue(id);
    }
    
    protected List<Positioned<Part>> getPositionedItems(List<Part> items) {
        List<Positioned<Part>> list = new ArrayList<>(items.size());
        int x = START_X;
        int y = START_Y;
    
        for (Part item : items) {
            list.add(new Positioned<>(x, y, item));
        
            x += OFFSET;
            if (x > MAX_X) {
                x = START_X;
                y += OFFSET;
            }
        }
        
        return list;
    }
    
    @Environment(EnvType.CLIENT)
    protected abstract boolean mayUseFluids();
    
    public boolean increaseItems(NonNullList<ItemStack> itemsToConsume, QuestDataTaskItems data, UUID playerId) {
        if (!parent.isAvailable(playerId)) return false;
        
        boolean updated = false;
        
        for (int i = 0; i < elements.size(); i++) {
            Part item = elements.get(i);
            if (!item.hasItem || data.isDone(i, item)) {
                continue;
            }
            
            for (int j = 0; j < itemsToConsume.size(); j++) {
                ItemStack stack = itemsToConsume.get(j);
                if (item.precision.areItemsSame(stack, item.stack)) {
                    int amount = Math.min(stack.getCount(), item.required - data.getValue(i));
                    if (amount > 0) {
                        stack.shrink(amount);
                        if (stack.getCount() == 0) {
                            itemsToConsume.set(j, ItemStack.EMPTY);
                        }
                        data.setValue(i, data.getValue(i) + amount);
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
        for (int i = 0; i < elements.size(); i++) {
            Part item = elements.get(i);
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
    public Class<QuestDataTaskItems> getDataType() {
        return QuestDataTaskItems.class;
    }
    
    @Override
    public QuestDataTaskItems newQuestData() {
        return new QuestDataTaskItems(elements.size());
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Positioned<Part>> items = getPositionedItems(getShownElements());
        
        for (int i = 0; i < items.size(); i++) {
            Positioned<Part> pos = items.get(i);
            Part item = pos.getElement();
            
            if (item.hasItem) {
                gui.drawItemStack(item.getPermutatedItem(), pos.getX(), pos.getY(), mX, mY, false);
            } else if (item.fluid != null) {
                gui.drawFluid(item.fluid, matrices, pos.getX(), pos.getY(), mX, mY);
            }
            
            FormattedText str = Translator.plain((getProgress(player, i) * 100 / item.required) + "%");
            matrices.pushPose();
            matrices.translate(0, 0, 200);// magic z value to write over stack render
            float textSize = 0.8F;
            gui.drawStringWithShadow(matrices, str, (int) (pos.getX() + SIZE - gui.getStringWidth(str) * textSize), (int) (pos.getY() + SIZE - (item.hasItem && !item.stack.isEmpty() && item.stack.getCount() != 1 ? TEXT_HEIGHT : 0) - TEXT_HEIGHT * textSize + 2), textSize, getProgress(player, i) == item.required ? 0x308030 : 0xFFFFFF);
            matrices.popPose();
        }
        
        for (int i = 0; i < items.size(); i++) {
            Positioned<Part> pos = items.get(i);
            Part item = pos.getElement();
            
            if (gui.inBounds(pos.getX(), pos.getY(), SIZE, SIZE, mX, mY)) {
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
            List<Positioned<Part>> items = getPositionedItems(getShownElements());
            
            for (int i = 0; i < items.size(); i++) {
                Positioned<Part> pos = items.get(i);
                Part item = pos.getElement();
                
                if (gui.inBounds(pos.getX(), pos.getY(), SIZE, SIZE, mX, mY)) {
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
                            if(mayUseFluids()) {
                                PickItemMenu.display(gui, player, item.hasItem ? Either.left(item.stack) : Either.right(item.fluid), PickItemMenu.Type.ITEM_FLUID, item.required, item.precision,
                                        result -> this.setItem(result.get(), result.getAmount(), result.getPrecision(), id));
                            } else {
                                PickItemMenu.display(gui, player, item.stack, PickItemMenu.Type.ITEM, item.required, item.precision,
                                        result -> this.setItem(Either.left(result.get()), result.getAmount(), result.getPrecision(), id));
                            }
                            
                        } else if (gui.getCurrentMode() == EditMode.DELETE && ((item.stack != null && !item.stack.isEmpty()) || item.fluid != null)) {
                            elements.remove(i);
                            SaveHelper.add(EditType.TASK_ITEM_REMOVE);
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
        QuestDataTaskItems data = getData(playerId);
        int done = 0;
        int total = 0;
        
        for (int i = 0; i < elements.size(); i++) {
            int req = elements.get(i).required;
            done += Math.min(req, data.getValue(i));
            total += req;
        }
        
        return Math.max(0, Math.min(1, done / (float) total));
    }
    
    @Override
    public void mergeProgress(UUID playerId, QuestDataTaskItems own, QuestDataTaskItems other) {
        own.merge(other);
        
        boolean completed = true;
        for (int i = 0; i < elements.size(); i++) {
            if (!own.isDone(i, elements.get(i))) {
                completed = false;
                break;
            }
        }
        
        if (completed) {
            completeTask(playerId);
        }
    }
    
    @Override
    public void autoComplete(UUID playerId, boolean status) {
        QuestDataTaskItems data = getData(playerId);
        if (status) {
            for (int i = 0; i < elements.size(); i++) {
                data.setValue(i, elements.get(i).required);
            }
        } else {
            for (int i = 0; i < elements.size(); i++) {
                data.setValue(i, 0);
            }
        }
    }
    
    @Override
    public void copyProgress(QuestDataTaskItems own, QuestDataTaskItems other) {
        own.update(other);
    }
    
    public static class Part {
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
        
        public Part(ItemStack stack, int required) {
            this.stack = stack;
            this.required = required;
            this.hasItem = true;
        }
        
        public Part(FluidStack fluid, int required) {
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
