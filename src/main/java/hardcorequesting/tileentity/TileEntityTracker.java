package hardcorequesting.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.ClientChange;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.client.interfaces.GuiWrapperEditMenu;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTracker;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityTracker extends TileEntity {


    private Quest quest;
    private String questId;
    private int radius;
    private TrackerType type = TrackerType.TEAM;

    private static final String NBT_QUEST = "Quest";
    private static final String NBT_RADIUS = "Radius";
    private static final String NBT_TYPE = "TrackerType";

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (quest != null) {
            compound.setString(NBT_QUEST, quest.getId());
        }
        compound.setInteger(NBT_RADIUS, radius);
        compound.setByte(NBT_TYPE, (byte) type.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey(NBT_QUEST)) {
            questId = compound.getString(NBT_QUEST);
        } else {
            quest = null;
        }
        radius = compound.getInteger(NBT_RADIUS);
        type = TrackerType.values()[compound.getByte(NBT_TYPE)];
    }

    private int delay = 0;

    @Override
    public void updateEntity() {
        if (quest == null && questId != null) {
            quest = Quest.getQuest(questId);
            questId = null;
        }

        if (!worldObj.isRemote && delay++ == 20) {
            if (quest != null && Quest.getQuest(quest.getId()) == null) {
                quest = null;
            }
            int oldMeta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            int meta = 0;
            if (quest != null) {
                meta = type.getMeta(this, quest, radius);
            }

            if (oldMeta != meta) {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 3);
                notifyUpdate(xCoord, yCoord, zCoord, 2);
            }

            delay = 0;
        }
    }

    private void notifyUpdate(int x, int y, int z, int i) {
        if (i == 2 || x != xCoord || y != yCoord || z != zCoord) {
            worldObj.notifyBlockOfNeighborChange(x, y, z, getBlockType());

            if (i > 0) {
                notifyUpdate(x - 1, y, z, i - 1);
                notifyUpdate(x + 1, y, z, i - 1);
                notifyUpdate(x, y - 1, z, i - 1);
                notifyUpdate(x, y + 1, z, i - 1);
                notifyUpdate(x, y, z - 1, i - 1);
                notifyUpdate(x, y, z + 1, i - 1);
            }
        }
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public TrackerType getType() {
        return type;
    }

    public void setType(TrackerType type) {
        this.type = type;
    }

    public void setCurrentQuest() {
        quest = Quest.getQuest(Quest.selectedQuestId);
    }

    public Quest getCurrentQuest() {
        return quest;
    }

    public void openInterface(EntityPlayer player) {
        if (player instanceof EntityPlayerMP)
            NetworkManager.sendToPlayer(GuiType.TRACKER.build(build()), (EntityPlayerMP) player);
    }

    private String[] build() {
        String[] data = new String[6];
        data[0] = "" + xCoord;
        data[1] = "" + yCoord;
        data[2] = "" + zCoord;
        data[3] = quest != null ? quest.getId() : null;
        data[5] = "" + radius;
        data[6] = "" + type.ordinal();
        return data;
    }

    private static TileEntityTracker getTracker(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        return (te instanceof TileEntityTracker) ? (TileEntityTracker) te : null;
    }

    @SideOnly(Side.CLIENT)
    public static void openInterface(EntityPlayer player, int x, int y, int z, String questId, int radius, TrackerType type) {
        TileEntityTracker tracker = getTracker(player.worldObj, x, y, z);
        if (tracker != null) {
            tracker.questId = questId;
            tracker.quest = null;
            tracker.radius = radius;
            tracker.type = type;
            GuiBase gui = new GuiWrapperEditMenu();
            gui.setEditMenu(new GuiEditMenuTracker(gui, player, tracker));
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
    }


    public void sendToServer() {
        NetworkManager.sendToServer(ClientChange.TRACKER_UPDATE.build(this));
    }

    public static void saveToServer(EntityPlayer player, int x, int y, int z, int radius, TrackerType type) {
        TileEntityTracker tracker = getTracker(player.worldObj, x, y, z);
        if (Quest.isEditing && tracker != null) {
            tracker.radius = radius;
            tracker.type = type;
        }
    }
}
