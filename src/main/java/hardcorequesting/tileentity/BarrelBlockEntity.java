package hardcorequesting.tileentity;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import hardcorequesting.blocks.DeliveryBlock;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskItems;
import hardcorequesting.quests.task.QuestTaskItemsConsume;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BarrelBlockEntity extends BaseBlockEntity implements Inventory, FluidInsertable {
    
    private static final String NBT_PLAYER_UUID = "Player";
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_TASK = "Task";
    private UUID selectedQuestId;
    public int selectedTask;
    private UUID playerId;
    
    public BarrelBlockEntity() {
        super(ModBlocks.typeBarrel);
    }
    
    @Override
    public int getInvSize() {
        return 1;
    }
    
    @Override
    public boolean isInvEmpty() {
        return false;
    }
    
    @NotNull
    @Override
    public ItemStack getInvStack(int i) {
        return ItemStack.EMPTY;
    }
    
    @NotNull
    @Override
    public ItemStack takeInvStack(int i, int j) {
        return ItemStack.EMPTY;
    }
    
    @NotNull
    @Override
    public ItemStack removeInvStack(int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public void setInvStack(int i, @NotNull ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task instanceof QuestTaskItemsConsume) {
            DefaultedList<ItemStack> list = DefaultedList.of();
            list.add(stack);
            if (((QuestTaskItemsConsume) task).increaseItems(list, (QuestDataTaskItems) task.getData(this.getPlayerUUID()), this.getPlayerUUID())) {
                this.updateState();
                this.doSync();
            }
        }
    }
    
    @Override
    public int getInvMaxStackAmount() {
        return 64;
    }
    
    @Override
    public boolean canPlayerUseInv(@NotNull PlayerEntity player) {
        return true;
    }
    
    @Override
    public void onInvOpen(@NotNull PlayerEntity player) {}
    
    @Override
    public void onInvClose(@NotNull PlayerEntity player) {}
    
    @Override
    public boolean isValidInvStack(int index, @NotNull ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task instanceof QuestTaskItemsConsume) {
            for (int i = 0; i < ((QuestTaskItemsConsume) task).getItems().length; i++) {
                QuestTaskItems.ItemRequirement requirement = ((QuestTaskItemsConsume) task).getItems()[i];
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
    public void clear() {}
    
    private void doSync() {
        if (!this.world.isClient) {
            // sync tile to client
            this.syncToClientsNearby();
            
            //sync the quest line progress
            QuestTask task = getCurrentTask();
            if (task != null) {
                PlayerEntity player = QuestingData.getPlayer(this.getPlayerUUID());
                if (player != null) {
                    task.getParent().sendUpdatedDataToTeam(player);
                }
            }
        }
    }
    
    private void updateState() {
        if (!this.world.isClient) {
            QuestTask task = this.getCurrentTask();
            boolean bound = false;
            if (task != null && !task.isCompleted(this.getPlayerUUID())) {
                bound = true;
            }
            world.setBlockState(pos, ModBlocks.blockBarrel.getDefaultState().with(DeliveryBlock.BOUND, bound), 3);
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
    
    public void storeSettings(PlayerEntity player) {
        this.setPlayerUUID(player.getUuid());
        QuestingData data = QuestingData.getQuestingData(this.getPlayerUUID());
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
    public FluidVolume attemptInsertion(FluidVolume fluidVolume, Simulation simulation) {
        QuestTask task = getCurrentTask();
        if (task instanceof QuestTaskItemsConsume) {
            if (((QuestTaskItemsConsume) task).increaseFluid(fluidVolume = fluidVolume.copy(), (QuestDataTaskItems) task.getData(this.getPlayerUUID()), this.getPlayerUUID())) {
                this.updateState();
                this.doSync();
            }
        }
        return fluidVolume;
    }
    
    @Override
    public void writeTile(CompoundTag nbt, NBTType type) {
        if (this.getPlayerUUID() != null && selectedQuestId != null) {
            nbt.putUuid(NBT_PLAYER_UUID, this.getPlayerUUID());
            nbt.putUuid(NBT_QUEST, this.getQuestUUID());
            nbt.putByte(NBT_TASK, (byte) selectedTask);
        }
    }
    
    @Override
    public void readTile(CompoundTag nbt, NBTType type) {
        if (nbt.contains(NBT_PLAYER_UUID + "Most")) {
            this.setPlayerUUID(nbt.getUuid(NBT_PLAYER_UUID));
            this.setQuestUUID(nbt.getUuid(NBT_QUEST));
            selectedTask = nbt.getByte(NBT_TASK);
        }
    }
    
}
