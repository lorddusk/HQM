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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class TileEntityPortal extends TileEntity implements IBlockSync, ITickable {

    public static final String NBT_QUEST = "Quest";
    public static final String NBT_TYPE = "PortalType";
    public static final String NBT_ID = "ItemId";
    public static final String NBT_DMG = "ItemDmg";
    public static final String NBT_COLLISION = "Collision";
    public static final String NBT_TEXTURES = "Textures";
    public static final String NBT_NOT_COLLISION = "NotCollision";
    public static final String NBT_NOT_TEXTURES = "NotTextures";
    private static final String QUEST = "quest";
    private static final String PORTAL_TYPE = "portalType";
    private static final String HAS_ITEM = "hasItem";
    private static final String ITEM = "fluidStack";
    private static final String ITEM_DAMAGE = "itemDamage";
    private static final String COMPLETED_COLLISION = "completedCollision";
    private static final String COMPLETED_TEXTURE = "completedTexture";
    private static final String UNCOMPLETED_COLLISION = "uncompletedCollision";
    private static final String UNCOMPLETED_TEXTURE = "uncompletedTexture";
    private static final String PLAYERS = "players";
    private Quest quest;
    private UUID questId;
    private List<UUID> players = new ArrayList<>();
    private PortalType type = PortalType.TECH;
    private ItemStack stack;
    private boolean completedTexture;
    private boolean uncompletedTexture = true;
    private boolean completedCollision;
    private boolean uncompletedCollision = true;
    private int delay = 20;
    private int resetDelay = 0;
    private boolean hasUpdatedData = false;

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
            compound.setUniqueId(NBT_QUEST, quest.getQuestId());
        }
        compound.setByte(NBT_TYPE, (byte) type.ordinal());
        if (stack != null) {
            compound.setShort(NBT_ID, (short) Item.getIdFromItem(stack.getItem()));
            compound.setShort(NBT_DMG, (short) stack.getItemDamage());
        }

        compound.setBoolean(NBT_COLLISION, completedCollision);
        compound.setBoolean(NBT_TEXTURES, completedTexture);
        compound.setBoolean(NBT_NOT_COLLISION, uncompletedCollision);
        compound.setBoolean(NBT_NOT_TEXTURES, uncompletedTexture);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readContentFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeContentToNBT(compound);
        return compound;
    }

    public void readContentFromNBT(NBTTagCompound compound) {
        // the following six lines are legacy code from the playername to UUID migration. can be removed in 1.14
        if(compound.hasKey(NBT_QUEST, Constants.NBT.TAG_STRING)){
            try{
                compound.setUniqueId(NBT_QUEST, UUID.fromString(compound.getString(NBT_QUEST)));
            } catch(IllegalArgumentException ignored){}
            compound.removeTag(NBT_QUEST);
        }
        if (compound.hasKey(NBT_QUEST + "Most")) {
            questId = compound.getUniqueId(NBT_QUEST);
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

            stack = new ItemStack(Item.getItemById(id), 1, dmg);
        } else {
            stack = null;
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

    @Override
    public void update() {
        if (!world.isRemote) {
            if (quest == null && questId != null) {
                quest = Quest.getQuest(questId);
                questId = null;
            }

            boolean updated = false;

            if (delay++ >= 20) {
                if (quest != null && Quest.getQuest(quest.getQuestId()) == null) {
                    quest = null;
                }

                if (quest != null) {
                    for (Team team : QuestingData.getAllTeams()) {
                        if (team.getQuestData(quest.getQuestId()).completed) {
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
                if (quest != null && Quest.getQuest(quest.getQuestId()) == null) {
                    quest = null;
                }

                if (quest != null) {
                    for(UUID uuid : new ArrayList<>(this.players)){
                        if (!QuestingData.hasData(uuid) || !quest.isCompleted(uuid)) {
                            this.players.remove(uuid);
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
        quest = Quest.getQuest(Quest.speciallySelectedQuestId);
        resetDelay = delay = 1200;
        NetworkManager.sendBlockUpdate(this, null, 0);
    }

    public Quest getCurrentQuest() {
        return quest;
    }

    @SideOnly(Side.CLIENT)
    private void keepClientDataUpdated() {
        double distance = Minecraft.getMinecraft().player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (distance > Math.pow(BLOCK_UPDATE_RANGE, 2)) {
            hasUpdatedData = false;
        } else if (!hasUpdatedData && distance < Math.pow(BLOCK_UPDATE_RANGE - BLOCK_UPDATE_BUFFER_DISTANCE, 2)) {
            hasUpdatedData = true;
            NetworkManager.sendBlockUpdate(this, Minecraft.getMinecraft().player, 0);
        }
    }

    @Override
    public void writeData(EntityPlayer player, boolean onServer, int type, JsonWriter writer) throws IOException {
        switch (type) {
            case 0:
                if (onServer) {
                    writer.name(QUEST).value(this.quest == null ? null : this.quest.getQuestId().toString());
                    writer.name(PORTAL_TYPE).value(this.type.ordinal());
                    if (!this.type.isPreset()) {
                        writer.name(HAS_ITEM).value(stack != null);
                        if (stack != null) {
                            writer.name(ITEM).value(stack.getItem().getRegistryName().toString());
                            writer.name(ITEM_DAMAGE).value(stack.getItemDamage());
                        }
                    }

                    writer.name(COMPLETED_COLLISION).value(completedCollision);
                    writer.name(COMPLETED_TEXTURE).value(completedTexture);
                    writer.name(UNCOMPLETED_COLLISION).value(uncompletedCollision);
                    writer.name(UNCOMPLETED_TEXTURE).value(uncompletedTexture);

                    writer.name(PLAYERS).beginArray();
                    for (UUID uuid : this.players){
                        writer.value(uuid.toString());
                    }
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
                        writer.name(HAS_ITEM).value(stack != null);
                        if (stack != null) {
                            writer.name(ITEM).value(stack.getItem().getRegistryName().toString());
                            writer.name(ITEM_DAMAGE).value(stack.getItemDamage());
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
                    this.quest = questElement.isJsonNull() ? null : Quest.getQuest(UUID.fromString(questElement.getAsString()));
                    this.type = PortalType.values()[data.get(PORTAL_TYPE).getAsInt()];
                    if (!this.type.isPreset()) {
                        if (data.get(HAS_ITEM).getAsBoolean()) {
                            String itemId = data.get(ITEM).getAsString();
                            int dmg = data.get(ITEM_DAMAGE).getAsInt();
                            stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(itemId)), 1, dmg);
                        } else {
                            stack = null;
                        }
                    }

                    completedCollision = data.get(COMPLETED_COLLISION).getAsBoolean();
                    completedTexture = data.get(COMPLETED_TEXTURE).getAsBoolean();
                    uncompletedCollision = data.get(UNCOMPLETED_COLLISION).getAsBoolean();
                    uncompletedTexture = data.get(UNCOMPLETED_TEXTURE).getAsBoolean();

                    players.clear();
                    for (JsonElement p : data.get(PLAYERS).getAsJsonArray()){
                        try{
                            UUID uuid = UUID.fromString(p.getAsString());
                            this.players.add(uuid);
                        } catch(IllegalArgumentException e){
                            e.printStackTrace(); // todo proper exception
                        }
                    }
                    
                    IBlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                break;
            case 1:
                if (onServer) {
                    if (Quest.canQuestsBeEdited(player)) {
                        this.type = PortalType.values()[data.get(PORTAL_TYPE).getAsInt()];
                        if (!this.type.isPreset()) {
                            if (data.get(HAS_ITEM).getAsBoolean()) {
                                String itemId = data.get(ITEM).getAsString();
                                int dmg = data.get(ITEM_DAMAGE).getAsInt();
                                stack = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(itemId)), 1, dmg);
                            } else {
                                stack = ItemStack.EMPTY;
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
        NetworkManager.sendBlockUpdate(this, Minecraft.getMinecraft().player, 1);
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }


    public TileEntityPortal copy() {
        TileEntityPortal portal = new TileEntityPortal();

        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        portal.readFromNBT(compound);
        portal.world = this.world;

        return portal;
    }
}
