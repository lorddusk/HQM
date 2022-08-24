package hardcorequesting.common.quests.task.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.UUID;

public abstract class ItemRequirementTask extends QuestTask<ItemsTaskData> {
    
    private static final String ITEMS = "items";
    
    protected static final int LIMIT = 5 * 7;
    
    protected final PartList<Part> parts = new PartList<>(Part::new, EditType.Type.TASK_ITEM, LIMIT);
    
    public ItemRequirementTask(TaskType<? extends ItemRequirementTask> type, Quest parent) {
        super(type, ItemsTaskData.class, parent);
    }
    
    public PartList<Part> getParts() {
        return parts;
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
    
    @Environment(EnvType.CLIENT)
    public void setItem(Either<ItemStack, FluidStack> stack, int amount, ItemPrecision precision, int id) {
        
        Part requirement = parts.getOrCreateForModify(id);
    
        requirement.stack = stack;
        requirement.required = amount;
        requirement.precision = precision;
        requirement.permutations = null;
        
        parent.setIconIfEmpty(stack);
    }
    
    public int getProgress(UUID playerId, int id) {
        if (id >= parts.size()) {
            return 0;
        }
        
        return getData(playerId).getValue(id);
    }
    
    @Environment(EnvType.CLIENT)
    public abstract boolean mayUseFluids();
    
    public boolean increaseItems(NonNullList<ItemStack> itemsToConsume, UUID playerId) {
        if (!parent.isAvailable(playerId)) return false;
    
        ItemsTaskData data = getData(playerId);
    
        boolean updated = false;
        
        for (int i = 0; i < parts.size(); i++) {
            Part item = parts.get(i);
            if (!item.hasItem() || data.isDone(i, item)) {
                continue;
            }
            
            for (int j = 0; j < itemsToConsume.size(); j++) {
                ItemStack stack = itemsToConsume.get(j);
                if (item.isStack(stack)) {
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
    
    public void switchPartStatus(int id, UUID playerId) {
        if (id >= 0 && id < parts.size()) {
            Part part = parts.get(id);
            ItemsTaskData qData = getData(playerId);
            if (qData.isDone(id, part)) {
                qData.setValue(id, 0);
                qData.completed = false;
                QuestingDataManager.getInstance().getQuestingData(playerId).getTeam().refreshData();
            } else {
                qData.setValue(id, part.required);
                doCompletionCheck(qData, playerId);
            }
            parent.sendUpdatedDataToTeam(playerId);
        }
    }
    
    @Override
    public void copyProgress(ItemsTaskData own, ItemsTaskData other) {
        own.update(other);
    }
    
    public static class Part {
        private static int CYCLE_TIME = 2;//2 second cycle
        
        public Either<ItemStack, FluidStack> stack;
        public int required;
        private ItemPrecision precision = ItemPrecision.PRECISE;
        private ItemStack[] permutations;
        private int cycleAt = -1;
        private int current = 0;
        private int last;
        
        private Part() {
            this(ItemStack.EMPTY, 1);
        }
        
        public Part(ItemStack stack, int required) {
            this.stack = Either.left(stack);
            this.required = required;
        }
        
        public Part(FluidStack fluid, int required) {
            this.stack = Either.right(fluid);
            this.required = required;
        }
        
        public ItemPrecision getPrecision() {
            return precision;
        }
        
        public void setPrecision(ItemPrecision precision) {
            this.precision = precision;
            permutations = null;
        }
        
        public boolean hasItem() {
            return stack.left().isPresent();
        }
        
        public ItemStack getStack() {
            return stack.left().orElse(ItemStack.EMPTY);
        }
        
        public boolean isStack(ItemStack otherStack) {
            return stack.left().map(itemStack -> getPrecision().areItemsSame(itemStack, otherStack)).orElse(false);
        }
        
        public boolean isFluid(Fluid fluid) {
            return stack.right().map(fluidStack -> fluidStack.getFluid() == fluid).orElse(false);
        }
        
        public void setItemStack(ItemStack stack) {
            this.stack = Either.left(stack);
            this.permutations = null;
        }

        public void setFluidStack(FluidStack stack) {
            this.stack = Either.right(stack);
        }
        
        private void setPermutations() {
            stack.ifLeft(itemStack -> {
                permutations = precision.getPermutations(itemStack);
                if (permutations != null && permutations.length > 0) {
                    last = permutations.length - 1;
                    cycleAt = -1;
                }
            });
        }
        
        public ItemStack getPermutatedItem() {
            if (permutations == null && precision.hasPermutations())
                setPermutations();
            if (permutations == null || permutations.length < 2)
                return stack.left().orElse(ItemStack.EMPTY);
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
