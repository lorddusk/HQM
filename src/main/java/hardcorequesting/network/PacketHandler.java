package hardcorequesting.network;

import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.*;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.GuiReward;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestTask;
import hardcorequesting.tileentity.IBlockSync;
import hardcorequesting.tileentity.TileEntityPortal;
import hardcorequesting.tileentity.TileEntityTracker;
//import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class PacketHandler {

    public static final double BLOCK_UPDATE_RANGE = 128;
    public static final int BLOCK_UPDATE_BUFFER_DISTANCE = 5;
    public static final int BIT_MASK[] = {
            0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
            0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff, 0x7fff, 0xffff,
            0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff,
            0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff,
            0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, 0xffffffff
    };

    public static DataWriter getWriter(PacketId id) {
        DataWriter dw = new DataWriter();
        dw.writeByte(1); //amount of packets, defaults to 1. Hast obe a byte so it's easy to modify afterwards
        dw.writeData(id.getId(), DataBitHelper.PACKET_ID);
        return dw;
    }

    private static final Map<String, String> nameOverride = new HashMap<String, String>();
    private static final Map<String, String> nameOverrideReversed = new HashMap<String, String>();
    private static String clientNameOverride;

    public static void add(EntityPlayer player, String name) {
        if (name != null) {
            String playerName = player.getGameProfile().getName();
            if (!playerName.equals(name)) {
                nameOverride.put(playerName, name);
                nameOverrideReversed.put(name, playerName);
                return;
            }
        }

        setBookState(player, true);
    }

    public static void remove(EntityPlayer player) {
        String name = nameOverride.remove(player.getGameProfile().getName());
        if (name != null) {
            nameOverrideReversed.remove(name);
        } else {
            setBookState(player, false);
        }
    }

    public static String getOverriddenName(String name) {
        return clientNameOverride != null ? clientNameOverride : nameOverride.get(name);
    }

    public static boolean canOverride(String name) {
        return !nameOverrideReversed.containsKey(name);
    }

    public static String getOverriddenBy(String name) {
        return nameOverrideReversed.get(name);
    }

    private static void setBookState(EntityPlayer player, boolean state) {
        Team team = QuestingData.getQuestingData(player).getTeam();
        for (Team.PlayerEntry entry : team.getPlayers()) {
            if (entry.isInTeam() && entry.getName().equals(QuestingData.getUserName(player))) {
                entry.setBookOpen(state);
                break;
            }
        }
    }

    public static void reset() {
        nameOverride.clear();
        nameOverrideReversed.clear();
        clientNameOverride = null;
    }

    public static void closeInterface() {
        sendToServer(getWriter(PacketId.CLOSE_INTERFACE));
    }

    private int missingPackets;
    private byte[][] data;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        onPacketData(event.packet.payload().array(), FMLClientHandler.instance().getClient().thePlayer, false);
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        onPacketData(event.packet.payload().array(), ((NetHandlerPlayServer) event.handler).playerEntity, true);
    }

    private void onPacketData(byte[] incomingData, EntityPlayer player, boolean onServer) {

        DataReader dr = null;
        try {
            dr = getDataReaderForPacket(incomingData);
            if (dr == null) {
                return;
            }

            //read the packet count that was manually read before the creation of the data reader
            dr.readByte();

            PacketId id = PacketId.getFromId((byte) dr.readData(DataBitHelper.PACKET_ID));

            switch (id) {
                case OPEN_INTERFACE:
                    boolean isOpBook = dr.readBoolean();
                    if (isOpBook) {
                        String openName = dr.readString(DataBitHelper.NAME_LENGTH);
                        clientNameOverride = openName.equals(player.getGameProfile().getName()) ? null : openName;
                    }
                    QuestingData.getQuestingData(player).receiveDataFromServer(dr);
                    GuiQuestBook.displayGui(player, isOpBook);
                    break;
                case QUEST_DATA:
                    handleQuestData(player, dr);
                    break;
                case TASK_REQUEST:
                    handleTaskRequest(player, dr);
                    break;
                case CLAIM_REWARD:
                    handleRewardClaim(player, dr);
                    break;
                case SELECT_TASK:
                    handleSelectTask(player, dr);
                    break;
                case SOUND:
                    SoundHandler.handleSoundPacket(dr);
                    break;
                case LORE:
                    SoundHandler.handleLorePacket(player, dr);
                    break;
                case TEAM:
                    Team.handlePacket(player, dr, onServer);
                    break;
                case REFRESH_INTERFACE:
                    QuestingData.getQuestingData(player).receiveDataFromServer(dr);
                    break;
                case CLOSE_INTERFACE:
                    remove(player);
                    break;
                case REFRESH_TEAM:
                    QuestingData.getQuestingData(player).getTeam().onPacket(dr);
                    break;
                case OP_BOOK:
                    OPBookHelper.handlePacket(player, dr);
                    break;
                case QUEST_SYNC:
                    QuestLine.receiveServerSync(dr);
                    GuiEditMenuItem.Search.initItems();
                    break;
                case BAG_INTERFACE:
                    GuiReward.open(player, dr);
                    break;
                case DEATH_STATS_UPDATE:
                    DeathStats.handlePacket(player, dr);
                    break;
                case TEAM_STATS_UPDATE:
                    TeamStats.handlePacket(player, dr);
                    break;
                case TRACKER_ACTIVATE:
                    TileEntityTracker.openInterface(player, dr);
                    break;
                case TRACKER_RESPONSE:
                    TileEntityTracker.saveToServer(player, dr);
                    break;
                case BLOCK_SYNC:
                    handleBlockSync(player, dr);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (dr != null) {
                dr.close();
            }
        }
    }

    private DataReader getDataReaderForPacket(byte[] incomingData) throws Throwable {
        if (missingPackets == 0) {
            int count = incomingData[0];
            if (count == 1) {
                return new DataReader(incomingData);
            } else {
                missingPackets = incomingData[0];
                if (missingPackets < 0) {
                    missingPackets += 256;
                }
                this.data = new byte[missingPackets][];
            }
        }


        this.data[incomingData.length - missingPackets--] = incomingData;

        if (missingPackets != 0) {
            return null;
        } else {
            int byteCount = 0;
            for (byte[] bytes : this.data) {
                byteCount += bytes.length;
            }
            byte[] packetData = new byte[byteCount];
            int start = 0;
            for (byte[] bytes : this.data) {
                int len = bytes.length;
                System.arraycopy(bytes, 0, packetData, start, len);
                start += len;
            }

            return new DataReader(packetData);
        }
    }


    private void handleSelectTask(EntityPlayer player, DataReader dr) {
        int questId = dr.readData(DataBitHelper.QUESTS);
        int taskId = dr.readData(DataBitHelper.TASKS);
        Quest quest = Quest.getQuest(questId);
        if (quest != null && taskId >= 0 && taskId < quest.getTasks().size()) {
            QuestingData data = QuestingData.getQuestingData(player);
            data.selectedQuest = questId;
            data.selectedTask = taskId;
        }
    }

    private void handleRewardClaim(EntityPlayer player, DataReader dr) {
        int questId = dr.readData(DataBitHelper.QUESTS);
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            quest.claimReward(player, dr);
        }
    }

    private void handleTaskRequest(EntityPlayer player, DataReader dr) {
        int questId = dr.readData(DataBitHelper.QUESTS);
        int taskId = dr.readData(DataBitHelper.TASKS);
        Quest quest = Quest.getQuest(questId);
        if (quest != null && taskId >= 0 && taskId < quest.getTasks().size()) {
            QuestTask task = quest.getTasks().get(taskId);
            task.onUpdate(player, dr);
        }
    }

    private void handleQuestData(EntityPlayer player, DataReader dr) {
        int questId = dr.readData(DataBitHelper.QUESTS);
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            quest.preRead(dr.readData(DataBitHelper.PLAYERS), quest.getQuestData(player));
            quest.read(dr, quest.getQuestData(player), QuestingData.FILE_VERSION, true);
        }
    }


    public static void sendToPlayer(String name, DataWriter dw) {
        if (!Quest.isEditing || QuestingData.isSinglePlayer()) {
            sendToBookPlayer(name, dw);
        }
        dw.close();
    }

    private static void sendToBookPlayer(String name, DataWriter dw) {
        if (QuestingData.getQuestingData(name).getTeam().getEntry(name).isBookOpen()) {
            EntityPlayer player = QuestingData.getPlayer(name);
            if (player != null) {
                dw.sendToPlayer((EntityPlayerMP) player);
            }
        }

        String playerName = nameOverrideReversed.get(name);
        if (playerName != null) {
            EntityPlayer other = QuestingData.getPlayer(playerName);
            if (other != null) {
                dw.sendToPlayer((EntityPlayerMP) other);
            }
        }
    }

    public static void sendToRawPlayer(EntityPlayer player, DataWriter dw) {
        if (!Quest.isEditing || QuestingData.isSinglePlayer()) {
            dw.sendToPlayer((EntityPlayerMP) player);
        }
        dw.close();
    }

    public static void sendToAllPlayers(DataWriter dw) {
        if (!Quest.isEditing && !QuestingData.isSinglePlayer()) {
            dw.sendToAllPlayers();
        }
        dw.close();
    }

    public static void sendToServer(DataWriter dw) {
        if (!Quest.isEditing || QuestingData.isSinglePlayer()) {
            dw.sendToServer();
        }
        dw.close();
    }


    public static void sendToAllPlayersWithOpenBook(DataWriter dw) {
        if (!Quest.isEditing || QuestingData.isSinglePlayer()) {
            for (String name : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getAllUsernames()) {
                sendToBookPlayer(name, dw);
            }
        }
        dw.close();
    }

    public static void sendBlockPacket(IBlockSync block, EntityPlayer player, int id) {
        if (block instanceof TileEntity) {
            DataWriter dw = getWriter(PacketId.BLOCK_SYNC);
            TileEntity te = (TileEntity) block;
            boolean onServer = !te.getWorld().isRemote;

            dw.writeData(te.getPos().getX(), DataBitHelper.WORLD_COORDINATE);
            dw.writeData(te.getPos().getY(), DataBitHelper.WORLD_COORDINATE);
            dw.writeData(te.getPos().getZ(), DataBitHelper.WORLD_COORDINATE);
            dw.writeData(id, block.infoBitLength());

            block.writeData(dw, player, onServer, id);

            if (!onServer) {
                dw.sendToServer();
            } else if (player != null) {
                dw.sendToPlayer((EntityPlayerMP) player);
            } else {
                dw.sendToAllPlayersAround(te, BLOCK_UPDATE_RANGE);
            }

            dw.close();
        }
    }

    private void handleBlockSync(EntityPlayer player, DataReader dr) {
        boolean onServer = !player.worldObj.isRemote;
        int x = dr.readData(DataBitHelper.WORLD_COORDINATE);
        int y = dr.readData(DataBitHelper.WORLD_COORDINATE);
        int z = dr.readData(DataBitHelper.WORLD_COORDINATE);

        TileEntity te = player.worldObj.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof IBlockSync) {
            IBlockSync block = (IBlockSync) te;
            int id = dr.readData(block.infoBitLength());

            block.readData(dr, player, onServer, id);
        }
    }
}
