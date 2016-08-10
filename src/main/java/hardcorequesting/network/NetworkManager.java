package hardcorequesting.network;

import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import hardcorequesting.ModInformation;
import hardcorequesting.network.message.BlockSyncMessage;
import hardcorequesting.network.message.BlockSyncMessageClient;
import hardcorequesting.network.message.*;
import hardcorequesting.tileentity.IBlockSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;
import java.io.StringWriter;

public class NetworkManager {
    private static final SimpleNetworkWrapper WRAPPER = new SimpleNetworkWrapper(ModInformation.CHANNEL);
    private static int id = 0;

    public static void init() {
        WRAPPER.registerMessage(OpenGuiMessage.Handler.class, OpenGuiMessage.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(FullSyncMessage.Handler.class, FullSyncMessage.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(TeamStatsMessage.Handler.class, TeamStatsMessage.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(TeamErrorMessage.Handler.class, TeamErrorMessage.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(QuestDataUpdateMessage.Handler.class, QuestDataUpdateMessage.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(DeathStatsMessage.Handler.class, DeathStatsMessage.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(TeamUpdateMessage.Handler.class, TeamUpdateMessage.class, id++, Side.CLIENT);

        WRAPPER.registerMessage(BlockSyncMessageClient.Handler.class, BlockSyncMessageClient.class, id++, Side.CLIENT);
        WRAPPER.registerMessage(BlockSyncMessage.Handler.class, BlockSyncMessage.class, id++, Side.SERVER);

        WRAPPER.registerMessage(TeamMessage.Handler.class, TeamMessage.class, id++, Side.SERVER);
        WRAPPER.registerMessage(ClientUpdateMessage.Handler.class, ClientUpdateMessage.class, id++, Side.SERVER);
        WRAPPER.registerMessage(OpActionMessage.Handler.class, OpActionMessage.class, id++, Side.SERVER);
    }

    public static void sendToPlayer(IMessage message, EntityPlayerMP player) {
        WRAPPER.sendTo(message, player);
    }

    public static void sendToAllPlayers(IMessage message) {
        WRAPPER.sendToAll(message);
    }

    public static void sendToServer(IMessage message) {
        WRAPPER.sendToServer(message);
    }

    public static void sendToPlayersAround(IMessage message, TileEntity te, double radius) {
        WRAPPER.sendToAllAround(message, asTarget(te, radius));
    }

    public static NetworkRegistry.TargetPoint asTarget(TileEntity te, double radius) {
        return new NetworkRegistry.TargetPoint(
                te.getWorld().provider.dimensionId,
                te.xCoord,
                te.yCoord,
                te.zCoord,
                radius
        );
    }

    public static <T extends TileEntity & IBlockSync> void sendBlockUpdate(T block, EntityPlayer player, int type) {
        StringWriter data = new StringWriter();
        boolean onServer = !block.getWorld().isRemote;
        try {
            JsonWriter writer = new JsonWriter(data);
            writer.beginObject();
            block.writeData(player, onServer, type, writer);
            writer.endObject();
            writer.close();
        } catch (IOException e) {
            return;
        }

        if (!onServer) {
            sendToServer(new BlockSyncMessageClient(block, type, data.toString()));
        } else {
            IMessage message = new BlockSyncMessage(block, type, data.toString());
            if (player instanceof EntityPlayerMP) {
                sendToPlayer(message, (EntityPlayerMP) player);
            } else {
                sendToPlayersAround(message, block, IBlockSync.BLOCK_UPDATE_RANGE);
            }
        }
    }
}
