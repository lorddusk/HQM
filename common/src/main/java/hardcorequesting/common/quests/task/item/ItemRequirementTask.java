package hardcorequesting.common.quests.task.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.client.ItemTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public abstract class ItemRequirementTask extends QuestTask<ItemsTaskData> {
    
    private static final String ITEMS = "items";
    
    protected static final int LIMIT = 5 * 7;
    
    public final PartList<Part> parts = new PartList<>(Part::new, EditType.Type.TASK_ITEM, LIMIT);
    private final TaskGraphic graphic = new ItemTaskGraphic(this);
    
    public ItemRequirementTask(Quest parent, String description, String longDescription) {
        super(ItemsTaskData.class, parent, description, longDescription);
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(ITEMS, parts.write(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, ITEMS, new JsonArray()), QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER);
    }
    
    @Deprecated
    public List<Part> getItems() {
        return parts.getElements();
    }
    
    @Environment(EnvType.CLIENT)
    public void setItem(Either<ItemStack, FluidStack> item, int amount, ItemPrecision precision, int id) {
        
        Part requirement = parts.getOrCreateForModify(id);
    
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
    
    public int getProgress(Player player, int id) {
        if (id >= parts.size()) {
            return 0;
        }
        
        return getData(player).getValue(id);
    }
    
    @Environment(EnvType.CLIENT)
    public abstract boolean mayUseFluids();
    
    public boolean increaseItems(NonNullList<ItemStack> itemsToConsume, ItemsTaskData data, UUID playerId) {
        if (!parent.isAvailable(playerId)) return false;
        
        boolean updated = false;
        
        for (int i = 0; i < parts.size(); i++) {
            Part item = parts.get(i);
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
    
    public void doCompletionCheck(ItemsTaskData data, UUID playerId) {
        boolean isDone = true;
        for (int i = 0; i < parts.size(); i++) {
            Part item = parts.get(i);
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
    public ItemsTaskData newQuestData() {
        return new ItemsTaskData(parts.size());
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        graphic.draw(matrices, gui, player, mX, mY);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        graphic.onClick(gui, player, mX, mY, b);
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        ItemsTaskData data = getData(team);
        int done = 0;
        int total = 0;
        
        for (int i = 0; i < parts.size(); i++) {
            int req = parts.get(i).required;
            done += Math.min(req, data.getValue(i));
            total += req;
        }
        
        return Math.max(0, Math.min(1, done / (float) total));
    }
    
    @Override
    public void mergeProgress(UUID playerId, ItemsTaskData own, ItemsTaskData other) {
        own.merge(other);
        
        boolean completed = true;
        for (int i = 0; i < parts.size(); i++) {
            if (!own.isDone(i, parts.get(i))) {
                completed = false;
                break;
            }
        }
        
        if (completed) {
            completeTask(playerId);
        }
    }
    
    @Override
    public void setComplete(ItemsTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.setValue(i, parts.get(i).required);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(ItemsTaskData own, ItemsTaskData other) {
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
        
        private Part() {
            this(ItemStack.EMPTY, 1);
        }
        
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
