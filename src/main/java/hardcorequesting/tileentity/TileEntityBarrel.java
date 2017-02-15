package hardcorequesting.tileentity;

import hardcorequesting.blocks.BlockDelivery;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskItemsConsume;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileEntityBarrel extends TileEntity implements IInventory, IFluidHandler, ITickable {

    private static final int SYNC_TIME = 20;
    private static final String NBT_PLAYER_NAME = "Player";
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_TASK = "Task";
    public String selectedQuest;
    public int selectedTask;
    private int modifiedSyncTimer;
    private String playerUuid;

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        QuestTask task = getCurrentTask();
        if (task != null && task instanceof QuestTaskItemsConsume) {
            if (((QuestTaskItemsConsume) task).increaseItems(new ItemStack[]{stack}, (QuestDataTaskItems) task.getData(playerUuid), playerUuid) && modifiedSyncTimer <= 0) {
                modifiedSyncTimer = SYNC_TIME;
            }
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public void update() {
        if (modifiedSyncTimer > 0 && --modifiedSyncTimer == 0) {
            doSync();
            updateState();
        }
    }

    private void doSync() {
        if (!worldObj.isRemote) {
            QuestTask task = getCurrentTask();
            if (task != null) {
                EntityPlayer player = QuestingData.getPlayerFromUsername(playerUuid);
                if (player != null) {
                    task.getParent().sendUpdatedDataToTeam(player);
                }
            }

        }
    }

    private void updateState() {
        if (!worldObj.isRemote) {
            QuestTask task = getCurrentTask();
            if (task != null) {
                EntityPlayer player = QuestingData.getPlayer(playerUuid);
                if (player != null) {
                    if(task.isCompleted(player)){
                        worldObj.setBlockState(pos, ModBlocks.itemBarrel.getDefaultState(), 3);
                    } else {
                        if(!worldObj.getBlockState(pos).getValue(BlockDelivery.BOUND)){
                            worldObj.setBlockState(pos, ModBlocks.itemBarrel.getDefaultState().withProperty(BlockDelivery.BOUND, true), 3);
                        }
                    }
                }
            }
        }
    }

    public QuestTask getCurrentTask() {
        if (playerUuid != null && selectedQuest != null) {
            Quest quest = Quest.getQuest(selectedQuest);
            if (quest != null && selectedTask >= 0 && selectedTask < quest.getTasks().size()) {
                return quest.getTasks().get(selectedTask);
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public void storeSettings(EntityPlayer player) {
        if (modifiedSyncTimer > 0) {
            modifiedSyncTimer = 0;
            doSync();
        }

        playerUuid = QuestingData.getUserUUID(player);
        QuestingData data = QuestingData.getQuestingData(player);
        selectedQuest = data.selectedQuest;
        selectedTask = data.selectedTask;

        updateState();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey(NBT_PLAYER_NAME)) {
            playerUuid = compound.getString(NBT_PLAYER_NAME);
            selectedQuest = compound.getString(NBT_QUEST);
            selectedTask = compound.getByte(NBT_TASK);
        } else {
            playerUuid = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (playerUuid != null) {
            compound.setString(NBT_PLAYER_NAME, playerUuid);
            compound.setString(NBT_QUEST, selectedQuest);
            compound.setByte(NBT_TASK, (byte) selectedTask);
        }
        return compound;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return this.getCapability(capability, facing) != null;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    public String getPlayer() {
        return playerUuid;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }

        if (doFill) {
            QuestTask task = getCurrentTask();
            if (task != null && task instanceof QuestTaskItemsConsume) {
                if (((QuestTaskItemsConsume) task).increaseFluid(resource.copy(), (QuestDataTaskItems) task.getData(playerUuid), playerUuid) && modifiedSyncTimer <= 0) {
                    modifiedSyncTimer = SYNC_TIME;
                }
            }
        }

        return resource.amount;
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
}
