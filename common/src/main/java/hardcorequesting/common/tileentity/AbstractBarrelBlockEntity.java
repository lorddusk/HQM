package hardcorequesting.common.tileentity;

import hardcorequesting.common.blocks.DeliveryBlock;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import hardcorequesting.common.quests.task.ConsumeItemTask;
import hardcorequesting.common.quests.task.ItemRequirementTask;
import hardcorequesting.common.quests.task.QuestTask;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class AbstractBarrelBlockEntity extends AbstractBaseBlockEntity implements Container {
    
    private static final String NBT_PLAYER_UUID = "Player";
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_TASK = "Task";
    private UUID selectedQuestId;
    public int selectedTask;
    private UUID playerId;
    
    public AbstractBarrelBlockEntity() {
        super(ModBlocks.typeBarrel.get());
    }
    
    @Override
    public int getContainerSize() {
        return 1;
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @NotNull
    @Override
    public ItemStack getItem(int i) {
        return ItemStack.EMPTY;
    }
    
    @NotNull
    @Override
    public ItemStack removeItem(int i, int j) {
        return ItemStack.EMPTY;
    }
    
    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public void setItem(int i, @NotNull ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task instanceof ConsumeItemTask) {
            NonNullList<ItemStack> list = NonNullList.create();
            list.add(stack);
            if (((ConsumeItemTask) task).increaseItems(list, (QuestDataTaskItems) task.getData(this.getPlayerUUID()), this.getPlayerUUID())) {
                this.updateState();
                this.doSync();
            }
        }
    }
    
    @Override
    public int getMaxStackSize() {
        return 64;
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
    
    @Override
    public void startOpen(@NotNull Player player) {}
    
    @Override
    public void stopOpen(@NotNull Player player) {}
    
    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task instanceof ConsumeItemTask) {
            ConsumeItemTask consumeTask = (ConsumeItemTask) task;
            for (int i = 0; i < consumeTask.getItems().size(); i++) {
                ItemRequirementTask.ItemRequirement requirement = consumeTask.getItems().get(i);
                if (requirement.hasItem && requirement.getPrecision().areItemsSame(requirement.getStack(), stack)) {
                    QuestDataTaskItems data = (QuestDataTaskItems) task.getData(this.getPlayerUUID());
                    if (data.progress.length > i) {
                        return data.progress[i] < requirement.required;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public void clearContent() {}
    
    protected void doSync() {
        if (!this.level.isClientSide) {
            // sync tile to client
            this.syncToClientsNearby();
            
            //sync the quest line progress
            QuestTask task = getCurrentTask();
            if (task != null) {
                Player player = QuestingData.getPlayer(this.getPlayerUUID());
                if (player != null) {
                    task.getParent().sendUpdatedDataToTeam(player);
                }
            }
        }
    }
    
    protected void updateState() {
        if (!this.level.isClientSide) {
            QuestTask task = this.getCurrentTask();
            boolean bound = false;
            if (task != null && !task.isCompleted(this.getPlayerUUID())) {
                bound = true;
            }
            level.setBlock(worldPosition, ModBlocks.blockBarrel.get().defaultBlockState().setValue(DeliveryBlock.BOUND, bound), 3);
        }
    }
    
    public QuestTask getCurrentTask() {
        if (this.getPlayerUUID() != null && selectedQuestId != null) {
            Quest quest = Quest.getQuest(selectedQuestId);
            if (quest != null && selectedTask >= 0 && selectedTask < quest.getTasks().size()) {
                return quest.getTasks().get(selectedTask);
            }
        }
        return null;
    }
    
    public void storeSettings(Player player) {
        this.setPlayerUUID(player.getUUID());
        QuestingData data = QuestingDataManager.getInstance().getQuestingData(this.getPlayerUUID());
        this.setQuestUUID(data.selectedQuestId);
        this.selectedTask = data.selectedTask;
        
        QuestTask task = this.getCurrentTask();
        if (task == null || task.isCompleted(this.getPlayerUUID())) {
            this.setPlayerUUID(null);
            this.setQuestUUID(null);
            this.selectedTask = 0;
        }
        
        this.doSync();
        this.updateState();
    }
    
    public UUID getPlayerUUID() {
        return this.playerId;
    }
    
    public void setPlayerUUID(UUID playerId) {
        this.playerId = playerId;
    }
    
    public UUID getQuestUUID() {
        return this.selectedQuestId;
    }
    
    public void setQuestUUID(UUID questId) {
        this.selectedQuestId = questId;
    }

//    @Override
//    public int fill(FluidStack resource, boolean doFill) {
//        if (resource == null) {
//            return 0;
//        }
//        int oldAmount = resource.amount;
//        if (doFill) {
//            QuestTask task = getCurrentTask();
//            if (task instanceof QuestTaskItemsConsume) {
//                if (((QuestTaskItemsConsume) task).increaseFluid(resource.copy(), (QuestDataTaskItems) task.getData(this.getPlayerUUID()), this.getPlayerUUID())) {
//                    this.updateState();
//                    this.doSync();
//                    return oldAmount - resource.amount;
//                }
//            }
//        }
//        return 0;
//    }
    
    @Override
    public void writeTile(CompoundTag nbt, NBTType type) {
        if (this.getPlayerUUID() != null && selectedQuestId != null) {
            nbt.putUUID(NBT_PLAYER_UUID, this.getPlayerUUID());
            nbt.putUUID(NBT_QUEST, this.getQuestUUID());
            nbt.putByte(NBT_TASK, (byte) selectedTask);
        }
    }
    
    @Override
    public void readTile(CompoundTag nbt, NBTType type) {
        if (nbt.contains(NBT_PLAYER_UUID + "Most")) {
            this.setPlayerUUID(nbt.getUUID(NBT_PLAYER_UUID));
            this.setQuestUUID(nbt.getUUID(NBT_QUEST));
            selectedTask = nbt.getByte(NBT_TASK);
        }
    }
    
}
