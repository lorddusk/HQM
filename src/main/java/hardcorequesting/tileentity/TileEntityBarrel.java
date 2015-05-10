package hardcorequesting.tileentity;

import hardcorequesting.QuestingData;
import hardcorequesting.blocks.BlockInfo;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestDataTaskItems;
import hardcorequesting.quests.QuestTask;
import hardcorequesting.quests.QuestTaskItemsConsume;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StringUtils;
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
                if (((QuestTaskItemsConsume)task).increaseFluid(resource.copy(), (QuestDataTaskItems) task.getData(playerName), playerName) && modifiedSyncTimer <= 0) {
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
    public ItemStack getStackInSlotOnClosing(int i) {
        return null;
    }

    private static final int SYNC_TIME = 20;
    private int modifiedSyncTimer;
    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        QuestTask task = getCurrentTask();
        if (task != null && task instanceof QuestTaskItemsConsume) {
            if (((QuestTaskItemsConsume)task).increaseItems(new ItemStack[]{itemstack}, (QuestDataTaskItems) task.getData(playerName), playerName) && modifiedSyncTimer <= 0) {
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
                EntityPlayer player = QuestingData.getPlayer(playerName);
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
                EntityPlayer player = QuestingData.getPlayer(playerName);
                if (player != null) {
                    state = !task.isCompleted(player);
                }
            }
            boolean oldState = getBlockMetadata() == 1;
            if (state != oldState) {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, state ? 1 : 0, 3);
            }
        }
    }

    public QuestTask getCurrentTask() {
        if (playerName != null && selectedQuest >= 0 && selectedQuest < Quest.size()) {
            Quest quest = Quest.getQuest(selectedQuest);
            if (quest != null && selectedTask >= 0 && selectedTask < quest.getTasks().size()) {
                return quest.getTasks().get(selectedTask);
            }
        }

        return null;
    }

    @Override
    public String getInventoryName() {
        return BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME;
    }

    @Override
    public boolean hasCustomInventoryName() {
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
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }



    private String playerName;
    public int selectedQuest;
    public int selectedTask;

    public void storeSettings(EntityPlayer player) {
        if (modifiedSyncTimer > 0) {
            modifiedSyncTimer = 0;
            doSync();
        }

        playerName = QuestingData.getUserName(player);
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
            playerName = compound.getString(NBT_PLAYER_NAME);
            selectedQuest = compound.getShort(NBT_QUEST);
            selectedTask = compound.getByte(NBT_TASK);
        }else{
            playerName = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (playerName != null) {
            compound.setString(NBT_PLAYER_NAME, playerName);
            compound.setShort(NBT_QUEST, (short)selectedQuest);
            compound.setByte(NBT_TASK, (byte)selectedTask);
        }
    }

    public String getPlayer() {
        return playerName;
    }
}
