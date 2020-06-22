package hardcorequesting.network;

import com.google.gson.stream.JsonWriter;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.network.message.*;
import hardcorequesting.tileentity.IBlockSync;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager {
    
    private static final Identifier S2C = new Identifier(HardcoreQuesting.ID, "s2c");
    private static final Identifier C2S = new Identifier(HardcoreQuesting.ID, "c2s");
    private static int id = 0;
    private static final Map<Class<? extends IMessage>, Pair<Class<? extends IMessageHandler>, Integer>> PACKET_HANDLERS = new HashMap<>();
    
    public static void init() {
        registerMessage(OpenGuiMessage.Handler.class, OpenGuiMessage.class, id++, EnvType.CLIENT);
        registerMessage(CloseBookMessage.Handler.class, CloseBookMessage.class, id++, EnvType.CLIENT);
        
        registerMessage(QuestLineSyncMessage.Handler.class, QuestLineSyncMessage.class, id++, EnvType.CLIENT);
        registerMessage(PlayerDataSyncMessage.Handler.class, PlayerDataSyncMessage.class, id++, EnvType.CLIENT);
        
        registerMessage(TeamStatsMessage.Handler.class, TeamStatsMessage.class, id++, EnvType.CLIENT);
        registerMessage(TeamErrorMessage.Handler.class, TeamErrorMessage.class, id++, EnvType.CLIENT);
        registerMessage(QuestDataUpdateMessage.Handler.class, QuestDataUpdateMessage.class, id++, EnvType.CLIENT);
        registerMessage(DeathStatsMessage.Handler.class, DeathStatsMessage.class, id++, EnvType.CLIENT);
        registerMessage(TeamUpdateMessage.Handler.class, TeamUpdateMessage.class, id++, EnvType.CLIENT);
        registerMessage(SoundMessage.Handler.class, SoundMessage.class, id++, EnvType.CLIENT);
        registerMessage(LivesUpdate.Handler.class, LivesUpdate.class, id++, EnvType.CLIENT);
        
        registerMessage(BlockSyncMessageClient.Handler.class, BlockSyncMessageClient.class, id++, EnvType.CLIENT);
        registerMessage(BlockSyncMessage.Handler.class, BlockSyncMessage.class, id++, EnvType.SERVER);
        
        registerMessage(TeamMessage.Handler.class, TeamMessage.class, id++, EnvType.SERVER);
        registerMessage(ClientUpdateMessage.Handler.class, ClientUpdateMessage.class, id++, EnvType.SERVER);
        registerMessage(OpActionMessage.Handler.class, OpActionMessage.class, id++, EnvType.SERVER);
        
        registerMessage(SyncableTileMessage.class, SyncableTileMessage.class, id++, EnvType.CLIENT);
        registerMessage(GeneralUpdateMessage.class, GeneralUpdateMessage.class, id++, EnvType.CLIENT);
        registerMessage(GeneralUpdateMessage.class, GeneralUpdateMessage.class, id++, EnvType.SERVER);
        
        if (HardcoreQuesting.loadingSide == EnvType.CLIENT) {
            ClientSidePacketRegistry.INSTANCE.register(S2C, (packetContext, packetByteBuf) -> {
                int id = packetByteBuf.readInt();
                for (Map.Entry<Class<? extends IMessage>, Pair<Class<? extends IMessageHandler>, Integer>> entry : PACKET_HANDLERS.entrySet()) {
                    if (entry.getValue().getRight() == id) {
                        try {
                            IMessage message = entry.getKey().newInstance();
                            message.fromBytes(packetByteBuf, packetContext);
                            IMessageHandler<IMessage, ?> handler = entry.getKey() != entry.getValue().getLeft() ? entry.getValue().getLeft().newInstance() : (IMessageHandler<IMessage, ?>) message;
                            handler.onMessage(message, packetContext);
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                HardcoreQuesting.LOG.error("Invalid Packet ID: " + id);
            });
        }
        ServerSidePacketRegistry.INSTANCE.register(C2S, (packetContext, packetByteBuf) -> {
            int id = packetByteBuf.readInt();
            for (Map.Entry<Class<? extends IMessage>, Pair<Class<? extends IMessageHandler>, Integer>> entry : PACKET_HANDLERS.entrySet()) {
                if (entry.getValue().getRight() == id) {
                    try {
                        IMessage message = entry.getKey().newInstance();
                        message.fromBytes(packetByteBuf, packetContext);
                        IMessageHandler<IMessage, ?> handler = entry.getKey() != entry.getValue().getLeft() ? entry.getValue().getLeft().newInstance() : (IMessageHandler<IMessage, ?>) message;
                        handler.onMessage(message, packetContext);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            HardcoreQuesting.LOG.error("Invalid Packet ID: " + id);
        });
    }
    
    private static void registerMessage(Class<? extends IMessageHandler> handlerClass, Class<? extends IMessage> messageClass, int id, EnvType envType) {
        PACKET_HANDLERS.put(messageClass, new Pair<>(handlerClass, id));
    }
    
    public static void sendToPlayer(IMessage message, ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Pair<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getRight());
        message.toBytes(buf);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, S2C, buf);
    }
    
    public static void sendToAllPlayers(IMessage message) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Pair<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getRight());
        message.toBytes(buf);
        for (ServerPlayerEntity entity : HardcoreQuesting.getServer().getPlayerManager().getPlayerList()) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(entity, S2C, new PacketByteBuf(buf.copy()));
        }
    }
    
    public static void sendToServer(IMessage message) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Pair<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getRight());
        message.toBytes(buf);
        ClientSidePacketRegistry.INSTANCE.sendToServer(C2S, buf);
    }
    
    public static void sendToPlayersAround(IMessage message, BlockEntity te, double radius) {
        BlockPos pos = te.getPos();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        Pair<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getRight());
        message.toBytes(buf);
        Packet<?> packet = ServerSidePacketRegistry.INSTANCE.toPacket(S2C, buf);
        HardcoreQuesting.getServer().getPlayerManager().sendToAround(null, pos.getX(), pos.getY(), pos.getZ(), radius, te.getWorld().getRegistryKey(), packet);
    }
    
    public static <T extends BlockEntity & IBlockSync> void sendBlockUpdate(T block, PlayerEntity player, int type) {
        StringWriter data = new StringWriter();
        boolean onServer = !block.getWorld().isClient;
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
            if (player instanceof ServerPlayerEntity) {
                sendToPlayer(message, (ServerPlayerEntity) player);
            } else {
                sendToPlayersAround(message, block, IBlockSync.BLOCK_UPDATE_RANGE);
            }
        }
    }
    
    public static void sendSyncPacket(BlockEntity tile) {
        if (tile instanceof ISyncableTile && !tile.getWorld().isClient) {
            sendToPlayersAround(new SyncableTileMessage(tile), tile, 128D);
        }
    }
}
