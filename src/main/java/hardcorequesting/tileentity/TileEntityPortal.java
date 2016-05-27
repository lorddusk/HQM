package hardcorequesting.tileentity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiWrapperEditMenu;
import hardcorequesting.client.interfaces.edit.GuiEditMenuPortal;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("Duplicates")
public class TileEntityPortal extends TileEntity implements IBlockSync, ITickable {

    private Quest quest;
    private String questId;
    private List<String> players = new ArrayList<>();
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
            compound.setString(NBT_QUEST, quest.getId());
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
            questId = compound.getString(NBT_QUEST);
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
            if (quest == null && questId != null) {
                quest = Quest.getQuest(questId);
                questId = null;
            }

            boolean updated = false;

            if (delay++ >= 20) {
                if (quest != null && Quest.getQuest(quest.getId()) == null) {
                    quest = null;
                }

                if (quest != null) {
                    for (Team team : QuestingData.getAllTeams()) {
                        if (team.getQuestData(quest.getId()).completed) {
                            for (PlayerEntry entry : team.getPlayers()) {
                                if (entry.isInTeam() && !players.contains(entry.getUUID())) {
                                    players.add(entry.getUUID());
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
                NetworkManager.sendBlockUpdate(this, null, 0);
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
        NetworkManager.sendBlockUpdate(this, null, 0);
    }

    public Quest getCurrentQuest() {
        return quest;
    }

    private boolean hasUpdatedData = false;

    @SideOnly(Side.CLIENT)
    private void keepClientDataUpdated() {
        double distance = Minecraft.getMinecraft().thePlayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (distance > Math.pow(BLOCK_UPDATE_RANGE, 2)) {
            hasUpdatedData = false;
        } else if (!hasUpdatedData && distance < Math.pow(BLOCK_UPDATE_RANGE - BLOCK_UPDATE_BUFFER_DISTANCE, 2)) {
            hasUpdatedData = true;
            NetworkManager.sendBlockUpdate(this, Minecraft.getMinecraft().thePlayer, 0);
        }
    }

    private static final String QUEST = "quest";
    private static final String PORTAL_TYPE = "portalType";
    private static final String HAS_ITEM = "hasItem";
    private static final String ITEM = "item";
    private static final String ITEM_DAMAGE = "itemDamage";
    private static final String COMPLETED_COLLISION = "completedCollision";
    private static final String COMPLETED_TEXTURE = "completedTexture";
    private static final String UNCOMPLETED_COLLISION = "uncompletedCollision";
    private static final String UNCOMPLETED_TEXTURE = "uncompletedTexture";
    private static final String PLAYERS = "players";

    @Override
    public void writeData(EntityPlayer player, boolean onServer, int type, JsonWriter writer) throws IOException {
        switch (type) {
            case 0:
                if (onServer) {
                    writer.name(QUEST).value(this.quest == null ? null : this.quest.getId());
                    writer.name(PORTAL_TYPE).value(this.type.ordinal());
                    if (!this.type.isPreset()) {
                        writer.name(HAS_ITEM).value(item != null);
                        if (item != null) {
                            writer.name(ITEM).value(item.getItem().getRegistryName().toString());
                            writer.name(ITEM_DAMAGE).value(item.getItemDamage());
                        }
                    }

                    writer.name(COMPLETED_COLLISION).value(completedCollision);
                    writer.name(COMPLETED_TEXTURE).value(completedTexture);
                    writer.name(UNCOMPLETED_COLLISION).value(uncompletedCollision);
                    writer.name(UNCOMPLETED_TEXTURE).value(uncompletedTexture);

                    writer.name(PLAYERS).beginArray();
                    for (String p : players)
                        writer.value(p);
                    writer.endArray();
                } else {
                    //send empty packet, no info required
                }
                break;
            case 1:
                if (onServer) {
                    //empty
                } else {
                    writer.name(PORTAL_TYPE).value(this.type.ordinal());
                    if (!this.type.isPreset()) {
                        writer.name(HAS_ITEM).value(item != null);
                        if (item != null) {
                            writer.name(ITEM).value(item.getItem().getRegistryName().toString());
                            writer.name(ITEM_DAMAGE).value(item.getItemDamage());
                        }
                    }
                    writer.name(COMPLETED_COLLISION).value(completedCollision);
                    writer.name(COMPLETED_TEXTURE).value(completedTexture);
                    writer.name(UNCOMPLETED_COLLISION).value(uncompletedCollision);
                    writer.name(UNCOMPLETED_TEXTURE).value(uncompletedTexture);
                }
        }
    }

    @Override
    public void readData(EntityPlayer player, boolean onServer, int type, JsonObject data) {
        switch (type) {
            case 0:
                if (onServer) {
                    //respond by sending the data to the client that required it
                    NetworkManager.sendBlockUpdate(this, player, 0);
                } else {
                    JsonElement questElement = data.get(QUEST);
                    this.quest = questElement.isJsonNull() ? null : Quest.getQuest(questElement.getAsString());
                    this.type = PortalType.values()[data.get(PORTAL_TYPE).getAsInt()];
                    if (!this.type.isPreset()) {
                        if (data.get(HAS_ITEM).getAsBoolean()) {
                            String itemId = data.get(ITEM).getAsString();
                            int dmg = data.get(ITEM_DAMAGE).getAsInt();
                            item = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(itemId)), 1, dmg);
                        } else {
                            item = null;
                        }
                    }

                    completedCollision = data.get(COMPLETED_COLLISION).getAsBoolean();
                    completedTexture = data.get(COMPLETED_TEXTURE).getAsBoolean();
                    uncompletedCollision = data.get(UNCOMPLETED_COLLISION).getAsBoolean();
                    uncompletedTexture = data.get(UNCOMPLETED_TEXTURE).getAsBoolean();

                    players.clear();
                    for (JsonElement p : data.get(PLAYERS).getAsJsonArray())
                        players.add(p.getAsString());
                    IBlockState state = worldObj.getBlockState(pos);
                    worldObj.notifyBlockUpdate(pos, state, state, 3);
                }
                break;
            case 1:
                if (onServer) {
                    if (Quest.isEditing) {
                        this.type = PortalType.values()[data.get(PORTAL_TYPE).getAsInt()];
                        if (!this.type.isPreset()) {
                            if (data.get(HAS_ITEM).getAsBoolean()) {
                                String itemId = data.get(ITEM).getAsString();
                                int dmg = data.get(ITEM_DAMAGE).getAsInt();
                                item = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(itemId)), 1, dmg);
                            } else {
                                item = null;
                            }
                        }

                        completedCollision = data.get(COMPLETED_COLLISION).getAsBoolean();
                        completedTexture = data.get(COMPLETED_TEXTURE).getAsBoolean();
                        uncompletedCollision = data.get(UNCOMPLETED_COLLISION).getAsBoolean();
                        uncompletedTexture = data.get(UNCOMPLETED_TEXTURE).getAsBoolean();

                        NetworkManager.sendBlockUpdate(this, null, 0); //refresh the clients
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
        NetworkManager.sendBlockUpdate(this, player, 1);
    }

    @SideOnly(Side.CLIENT)
    public void sendToServer() {
        NetworkManager.sendBlockUpdate(this, Minecraft.getMinecraft().thePlayer, 1);
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
