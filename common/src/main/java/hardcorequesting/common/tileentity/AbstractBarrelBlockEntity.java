package hardcorequesting.common.tileentity;

import hardcorequesting.common.blocks.DeliveryBlock;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class AbstractBarrelBlockEntity extends BlockEntity implements Container {
    
    private static final String NBT_PLAYER_UUID = "Player";
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_TASK = "Task";
    private UUID selectedQuestId;
    public int selectedTask;
    private UUID playerId;
    
    public AbstractBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.typeBarrel.get(), pos, state);
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
        QuestTask<?> task = getCurrentTask();
        if (task instanceof ConsumeItemTask consumeTask) {
    
            NonNullList<ItemStack> list = NonNullList.create();
            list.add(stack);
            if (consumeTask.increaseItems(list, this.getPlayerUUID())) {
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
        QuestTask<?> task = getCurrentTask();
        if (task instanceof ConsumeItemTask) {
            return ((ConsumeItemTask) task).canTakeItem(stack, getPlayerUUID());
        }
        return false;
    }
    
    @Override
    public void clearContent() {}
    
    protected void doSync() {
        if (!this.level.isClientSide()) {
            // sync tile to client
            
            ServerLevel world = (ServerLevel) getLevel();
            world.getChunkSource().blockChanged(getBlockPos());
    
            //sync the quest line progress
            QuestTask<?> task = getCurrentTask();
            if (task != null) {
                Player player = QuestingData.getPlayer(this.getPlayerUUID());
                if (player != null) {
                    task.getParent().sendUpdatedDataToTeam(player);
                }
            }
        }
    }
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    
    protected void updateState() {
        if (!this.level.isClientSide) {
            QuestTask<?> task = this.getCurrentTask();
            boolean bound = false;
            if (task != null && !task.isCompleted(this.getPlayerUUID())) {
                bound = true;
            }
            level.setBlock(worldPosition, ModBlocks.blockBarrel.get().defaultBlockState().setValue(DeliveryBlock.BOUND, bound), 3);
        }
    }
    
    public QuestTask<?> getCurrentTask() {
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
        
        QuestTask<?> task = this.getCurrentTask();
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
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (this.getPlayerUUID() != null && selectedQuestId != null) {
            compoundTag.putUUID(NBT_PLAYER_UUID, this.getPlayerUUID());
            compoundTag.putUUID(NBT_QUEST, this.getQuestUUID());
            compoundTag.putByte(NBT_TASK, (byte) selectedTask);
        }
    }
    
    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains(NBT_PLAYER_UUID + "Most")) {
            this.setPlayerUUID(compoundTag.getUUID(NBT_PLAYER_UUID));
            this.setQuestUUID(compoundTag.getUUID(NBT_QUEST));
            selectedTask = compoundTag.getByte(NBT_TASK);
        }
    }
}
