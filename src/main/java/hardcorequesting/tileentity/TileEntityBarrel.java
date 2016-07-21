package hardcorequesting.tileentity;

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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityBarrel extends TileEntity implements IInventory, IFluidHandler {

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
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

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[0];
    }

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
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    private static final int SYNC_TIME = 20;
    private int modifiedSyncTimer;

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        QuestTask task = getCurrentTask();
        if (task != null && task instanceof QuestTaskItemsConsume) {
            if (((QuestTaskItemsConsume) task).increaseItems(new ItemStack[]{itemstack}, (QuestDataTaskItems) task.getData(playerUuid), playerUuid) && modifiedSyncTimer <= 0) {
                modifiedSyncTimer = SYNC_TIME;
            }
        }
    }

    @Override
    public void updateEntity() {
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
            boolean state = false;
            if (task != null) {
                EntityPlayer player = QuestingData.getPlayer(playerUuid);
                if (player != null) {
                    state = !task.isCompleted(player);
                }
            }
            boolean oldState = getBlockMetadata() == 1;
            if (state != oldState) {/*
                if (state) { // TODO add the actual states (meta 1 == has active quest set) / (meta 0 == no or completed quest set)
                    worldObj.setBlockState(pos, ModBlocks.itemBarrel.getDefaultState(), 3);
                } else {
                    worldObj.setBlockState(pos, ModBlocks.itemBarrel.getDefaultState(), 3);
                }*/
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, state ? 1 : 0, 3);
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

//    @Override
//    public String getInventoryName() {
//        return BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME;
//    }

    @Override
    public String getInventoryName() {
        return null;
    }

    @Override
    public boolean isCustomInventoryName() {
        return false;
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
    public void openChest() {

    }

    @Override
    public void closeChest() {

    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }

    private String playerUuid;
    public String selectedQuest;
    public int selectedTask;

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

    private static final String NBT_PLAYER_NAME = "Player";
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_TASK = "Task";

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
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (playerUuid != null) {
            compound.setString(NBT_PLAYER_NAME, playerUuid);
            compound.setString(NBT_QUEST, selectedQuest);
            compound.setByte(NBT_TASK, (byte) selectedTask);
        }
    }

    public String getPlayer() {
        return playerUuid;
    }
}
