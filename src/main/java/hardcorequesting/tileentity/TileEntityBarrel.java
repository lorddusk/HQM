package hardcorequesting.tileentity;

import hardcorequesting.blocks.BlockDelivery;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.network.ISyncableTile;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskItems;
import hardcorequesting.quests.task.QuestTaskItemsConsume;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityBarrel extends TileBase implements IInventory, IFluidHandler{

    private static final String NBT_PLAYER_UUID = "Player";
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_TASK = "Task";
    private UUID selectedQuestId;
    public int selectedTask;
    private UUID playerId;

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }
    
    @Nonnull
    @Override
    public ItemStack decrStackSize(int i, int j) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int i, @Nonnull ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task instanceof QuestTaskItemsConsume) {
            NonNullList<ItemStack> list = NonNullList.create();
            list.add(stack);
            if(((QuestTaskItemsConsume) task).increaseItems(list, (QuestDataTaskItems) task.getData(this.getPlayerUUID()), this.getPlayerUUID())){
                this.updateState();
                this.doSync();
            }
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {}

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task instanceof QuestTaskItemsConsume){
            for(int i = 0; i < ((QuestTaskItemsConsume) task).getItems().length; i++){
                QuestTaskItems.ItemRequirement requirement = ((QuestTaskItemsConsume) task).getItems()[i];
                if(requirement.hasItem && requirement.getPrecision().areItemsSame(requirement.getStack(), stack)){
                    QuestDataTaskItems data = (QuestDataTaskItems) task.getData(this.getPlayerUUID());
                    if(data.progress.length > i){
                        return data.progress[i] < requirement.required;
                    }else{
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}
    
    private void doSync() {
        if (!this.world.isRemote) {
            // sync tile to client
            this.syncToClientsNearby();
            
            //sync the quest line progress
            QuestTask task = getCurrentTask();
            if (task != null) {
                EntityPlayer player = QuestingData.getPlayer(this.getPlayerUUID());
                if (player != null) {
                    task.getParent().sendUpdatedDataToTeam(player);
                }
            }
        }
    }
    
    private void updateState() {
        if (!this.world.isRemote) {
            QuestTask task = this.getCurrentTask();
            boolean bound = false;
            if(task != null && !task.isCompleted(this.getPlayerUUID())){
                bound = true;
            }
            world.setBlockState(pos, ModBlocks.itemBarrel.getDefaultState().withProperty(BlockDelivery.BOUND, bound), 3);
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

    @Nonnull
    @Override
    public String getName() {
        return "QDS";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public void storeSettings(EntityPlayer player) {
        this.setPlayerUUID(player.getPersistentID());
        QuestingData data = QuestingData.getQuestingData(this.getPlayerUUID());
        this.setQuestUUID(data.selectedQuestId);
        this.selectedTask = data.selectedTask;
        
        QuestTask task = this.getCurrentTask();
        if(task == null || task.isCompleted(this.getPlayerUUID())){
            this.setPlayerUUID(null);
            this.setQuestUUID(null);
            this.selectedTask = 0;
        }
        
        this.doSync();
        this.updateState();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return this.getCapability(capability, facing) != null;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    public UUID getPlayerUUID() {
        return this.playerId;
    }
    
    public void setPlayerUUID(UUID playerId){
        this.playerId = playerId;
    }
    
    public UUID getQuestUUID(){
        return this.selectedQuestId;
    }
    
    public void setQuestUUID(UUID questId){
        this.selectedQuestId = questId;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill){
        if(resource == null){
            return 0;
        }
        int oldAmount = resource.amount;
        if(doFill){
            QuestTask task = getCurrentTask();
            if(task instanceof QuestTaskItemsConsume){
                if(((QuestTaskItemsConsume) task).increaseFluid(resource.copy(), (QuestDataTaskItems) task.getData(this.getPlayerUUID()), this.getPlayerUUID())){
                    this.updateState();
                    this.doSync();
                    return oldAmount - resource.amount;
                }
            }
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }
    
    @Override
    public void writeTile(NBTTagCompound nbt, NBTType type){
        if (this.getPlayerUUID() != null && selectedQuestId != null) {
            nbt.setUniqueId(NBT_PLAYER_UUID, this.getPlayerUUID());
            nbt.setUniqueId(NBT_QUEST, this.getQuestUUID());
            nbt.setByte(NBT_TASK, (byte) selectedTask);
        }
    }
    
    @Override
    public void readTile(NBTTagCompound nbt, NBTType type){
        // the following six lines are legacy code from the playername to UUID migration. can be removed in 1.14
        if(nbt.hasKey(NBT_PLAYER_UUID, Constants.NBT.TAG_STRING)){
            try{
                nbt.setUniqueId(NBT_PLAYER_UUID, UUID.fromString(nbt.getString(NBT_PLAYER_UUID)));
            } catch(IllegalArgumentException ignored){}
            nbt.removeTag(NBT_PLAYER_UUID);
        }
    
        // the following six lines are legacy code from the selectedQuest to UUID migration. can be removed in 1.14
        if(nbt.hasKey(NBT_QUEST, Constants.NBT.TAG_STRING)){
            try{
                nbt.setUniqueId(NBT_QUEST, UUID.fromString(nbt.getString(NBT_QUEST)));
            } catch(IllegalArgumentException ignored){}
            nbt.removeTag(NBT_QUEST);
        }
    
        if (nbt.hasKey(NBT_PLAYER_UUID + "Most")) {
            this.setPlayerUUID(nbt.getUniqueId(NBT_PLAYER_UUID));
            this.setQuestUUID(nbt.getUniqueId(NBT_QUEST));
            selectedTask = nbt.getByte(NBT_TASK);
        }
    }
    
}
