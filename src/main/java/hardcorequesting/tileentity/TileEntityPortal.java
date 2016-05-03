package hardcorequesting.tileentity;

import hardcorequesting.QuestingData;
import hardcorequesting.Team;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiEditMenuPortal;
import hardcorequesting.client.interfaces.GuiWrapperEditMenu;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.quests.Quest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import net.minecraft.util.IIcon;

public class TileEntityPortal extends TileEntity implements IBlockSync, ITickable {

    private Quest quest;
    private int questId = -1;
    private List<String> players = new ArrayList<String>();
    private PortalType type = PortalType.TECH;
    private ItemStack item;

    private boolean completedTexture;
    private boolean uncompletedTexture = true;
    private boolean completedCollision;
    private boolean uncompletedCollision = true;

    public static final String NBT_QUEST = "Quest";
    public static final String NBT_TYPE = "PortalType";
    public static final String NBT_ID = "ItemId";
    public static final String NBT_DMG = "ItemDmg";

    public static final String NBT_COLLISION = "Collision";
    public static final String NBT_TEXTURES = "Textures";
    public static final String NBT_NOT_COLLISION = "NotCollision";
    public static final String NBT_NOT_TEXTURES = "NotTextures";

    public boolean isCompletedTexture() {
        return completedTexture;
    }

    public void setCompletedTexture(boolean completedTexture) {
        this.completedTexture = completedTexture;
    }

    public boolean isUncompletedTexture() {
        return uncompletedTexture;
    }

    public void setUncompletedTexture(boolean uncompletedTexture) {
        this.uncompletedTexture = uncompletedTexture;
    }

    public boolean isCompletedCollision() {
        return completedCollision;
    }

    public void setCompletedCollision(boolean completedCollision) {
        this.completedCollision = completedCollision;
    }

    public boolean isUncompletedCollision() {
        return uncompletedCollision;
    }

    public void setUncompletedCollision(boolean uncompletedCollision) {
        this.uncompletedCollision = uncompletedCollision;
    }

    public PortalType getType() {
        return type;
    }

    public void setType(PortalType type) {
        this.type = type;
    }


    public void writeContentToNBT(NBTTagCompound compound) {
        if (quest != null) {
            compound.setShort(NBT_QUEST, quest.getId());
        }
        compound.setByte(NBT_TYPE, (byte) type.ordinal());
        if (item != null) {
            compound.setShort(NBT_ID, (short) Item.getIdFromItem(item.getItem()));
            compound.setShort(NBT_DMG, (short) item.getItemDamage());
        }

        compound.setBoolean(NBT_COLLISION, completedCollision);
        compound.setBoolean(NBT_TEXTURES, completedTexture);
        compound.setBoolean(NBT_NOT_COLLISION, uncompletedCollision);
        compound.setBoolean(NBT_NOT_TEXTURES, uncompletedTexture);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeContentToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readContentFromNBT(compound);
    }

    public void readContentFromNBT(NBTTagCompound compound) {
        if (compound.hasKey(NBT_QUEST)) {
            questId = compound.getShort(NBT_QUEST);
            if (Quest.getQuests() != null) {
                quest = Quest.getQuest(questId);
            }
        } else {
            quest = null;
        }

        type = PortalType.values()[compound.getByte(NBT_TYPE)];
        if (compound.hasKey(NBT_ID)) {
            int id = compound.getShort(NBT_ID);
            int dmg = compound.getShort(NBT_DMG);

            item = new ItemStack(Item.getItemById(id), 1, dmg);
        } else {
            item = null;
        }

        if (compound.hasKey(NBT_COLLISION)) {
            completedCollision = compound.getBoolean(NBT_COLLISION);
            completedTexture = compound.getBoolean(NBT_COLLISION);
            uncompletedCollision = compound.getBoolean(NBT_NOT_COLLISION);
            uncompletedTexture = compound.getBoolean(NBT_NOT_TEXTURES);
        } else {
            completedCollision = completedTexture = false;
            uncompletedCollision = uncompletedTexture = true;
        }
    }

    private int delay = 20;
    private int resetDelay = 0;

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            if (quest == null && questId != -1) {
                quest = Quest.getQuest(questId);
                questId = -1;
            }

            boolean updated = false;

            if (delay++ >= 20) {
                if (quest != null && Quest.getQuest(quest.getId()) == null) {
                    quest = null;
                }

                if (quest != null) {
                    for (Team team : QuestingData.getAllTeams()) {
                        if (team.getQuestData(quest.getId()).completed) {
                            for (Team.PlayerEntry entry : team.getPlayers()) {
                                if (entry.isInTeam() && !players.contains(entry.getName())) {
                                    players.add(entry.getName());
                                    updated = true;
                                }
                            }
                        }
                    }
                }

                delay = 0;
            }

            if (resetDelay++ >= 1200) {
                if (quest != null && Quest.getQuest(quest.getId()) == null) {
                    quest = null;
                }

                if (quest != null) {
                    for (Iterator<String> iterator = players.iterator(); iterator.hasNext(); ) {
                        String player = iterator.next();

                        if (!QuestingData.hasData(player) || !quest.isCompleted(player)) {
                            iterator.remove();
                            updated = true;
                        }
                    }
                } else if (players.size() > 0) {
                    players.clear();
                    updated = true;
                }


                resetDelay = 0;
            }

            if (updated) {
                PacketHandler.sendBlockPacket(this, null, 0);
            }
        } else {
            keepClientDataUpdated();
        }
    }

    public boolean hasTexture(EntityPlayer player) {
        return players.contains(player.getGameProfile().getName()) ? completedTexture : uncompletedTexture;
    }

    public boolean hasCollision(EntityPlayer player) {
        return players.contains(player.getGameProfile().getName()) ? completedCollision : uncompletedCollision;
    }

    public void setCurrentQuest() {
        quest = Quest.getQuest(Quest.selectedQuestId);
        resetDelay = delay = 1200;
        PacketHandler.sendBlockPacket(this, null, 0);
    }

    public Quest getCurrentQuest() {
        return quest;
    }

    private boolean hasUpdatedData = false;

    @SideOnly(Side.CLIENT)
    private void keepClientDataUpdated() {
        double distance = Minecraft.getMinecraft().thePlayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (distance > Math.pow(PacketHandler.BLOCK_UPDATE_RANGE, 2)) {
            hasUpdatedData = false;
        } else if (!hasUpdatedData && distance < Math.pow(PacketHandler.BLOCK_UPDATE_RANGE - PacketHandler.BLOCK_UPDATE_BUFFER_DISTANCE, 2)) {
            hasUpdatedData = true;
            PacketHandler.sendBlockPacket(this, Minecraft.getMinecraft().thePlayer, 0);
        }
    }

    @Override
    public void writeData(DataWriter dw, EntityPlayer player, boolean onServer, int id) {
        switch (id) {
            case 0:
                if (onServer) {
                    dw.writeBoolean(quest != null);
                    if (quest != null) {
                        dw.writeData(quest.getId(), DataBitHelper.QUESTS);
                    }
                    dw.writeData(type.ordinal(), DataBitHelper.PORTAL_TYPE);
                    if (!type.isPreset()) {
                        dw.writeBoolean(item != null);
                        if (item != null) {
                            dw.writeData(Item.getIdFromItem(item.getItem()), DataBitHelper.SHORT);
                            dw.writeData(item.getItemDamage(), DataBitHelper.SHORT);
                        }
                    }

                    dw.writeBoolean(completedCollision);
                    dw.writeBoolean(completedTexture);
                    dw.writeBoolean(uncompletedCollision);
                    dw.writeBoolean(uncompletedTexture);

                    dw.writeData(players.size(), DataBitHelper.PLAYERS);
                    for (String p : players) {
                        dw.writeString(p, DataBitHelper.NAME_LENGTH);
                    }
                } else {
                    //send empty packet, no info required
                }
                break;
            case 1:
                if (onServer) {
                    //empty
                } else {
                    dw.writeData(type.ordinal(), DataBitHelper.PORTAL_TYPE);
                    if (!type.isPreset()) {
                        dw.writeBoolean(item != null);
                        if (item != null) {
                            dw.writeData(Item.getIdFromItem(item.getItem()), DataBitHelper.SHORT);
                            dw.writeData(item.getItemDamage(), DataBitHelper.SHORT);
                        }
                    }
                    dw.writeBoolean(completedCollision);
                    dw.writeBoolean(completedTexture);
                    dw.writeBoolean(uncompletedCollision);
                    dw.writeBoolean(uncompletedTexture);
                }
        }
    }

    @Override
    public void readData(DataReader dr, EntityPlayer player, boolean onServer, int id) {
        switch (id) {
            case 0:
                if (onServer) {
                    //respond by sending the data to the client that required it
                    PacketHandler.sendBlockPacket(this, player, 0);
                } else {
                    if (dr.readBoolean()) {
                        quest = Quest.getQuest(dr.readData(DataBitHelper.QUESTS));
                    } else {
                        quest = null;
                    }
                    type = PortalType.values()[dr.readData(DataBitHelper.PORTAL_TYPE)];
                    if (!type.isPreset()) {
                        if (dr.readBoolean()) {
                            int itemId = dr.readData(DataBitHelper.SHORT);
                            int dmg = dr.readData(DataBitHelper.SHORT);
                            item = new ItemStack(Item.getItemById(itemId), 1, dmg);
                        } else {
                            item = null;
                        }
                    }

                    completedCollision = dr.readBoolean();
                    completedTexture = dr.readBoolean();
                    uncompletedCollision = dr.readBoolean();
                    uncompletedTexture = dr.readBoolean();

                    players.clear();
                    int count = dr.readData(DataBitHelper.PLAYERS);
                    for (int i = 0; i < count; i++) {
                        players.add(dr.readString(DataBitHelper.NAME_LENGTH));
                    }
                    IBlockState state = worldObj.getBlockState(pos);
                    worldObj.notifyBlockUpdate(pos, state, state, 3);
                }
                break;
            case 1:
                if (onServer) {
                    if (Quest.isEditing) {
                        type = PortalType.values()[dr.readData(DataBitHelper.PORTAL_TYPE)];
                        if (!type.isPreset()) {
                            if (dr.readBoolean()) {
                                int itemId = dr.readData(DataBitHelper.SHORT);
                                int dmg = dr.readData(DataBitHelper.SHORT);
                                item = new ItemStack(Item.getItemById(itemId), 1, dmg);
                            } else {
                                item = null;
                            }
                        }
                        completedCollision = dr.readBoolean();
                        completedTexture = dr.readBoolean();
                        uncompletedCollision = dr.readBoolean();
                        uncompletedTexture = dr.readBoolean();
                        PacketHandler.sendBlockPacket(this, null, 0); //refresh the clients
                    }
                } else {
                    openInterfaceClient(player);
                }
        }
    }

    @SideOnly(Side.CLIENT)
    private void openInterfaceClient(EntityPlayer player) {
        GuiBase gui = new GuiWrapperEditMenu();
        gui.setEditMenu(new GuiEditMenuPortal(gui, player, this));
        Minecraft.getMinecraft().displayGuiScreen(gui);
    }

    public void openInterface(EntityPlayer player) {
        PacketHandler.sendBlockPacket(this, player, 1);
    }

    @SideOnly(Side.CLIENT)
    public void sendToServer() {
        PacketHandler.sendBlockPacket(this, Minecraft.getMinecraft().thePlayer, 1);
    }

    @Override
    public int infoBitLength() {
        return 1;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }


    public TileEntityPortal copy() {
        TileEntityPortal portal = new TileEntityPortal();

        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        portal.readFromNBT(compound);
        portal.worldObj = this.worldObj;


        return portal;
    }
}
