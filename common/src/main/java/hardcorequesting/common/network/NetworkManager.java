package hardcorequesting.common.network;

import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.message.*;
import hardcorequesting.common.tileentity.IBlockSync;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager {
    
    private static final ResourceLocation S2C = new ResourceLocation(HardcoreQuestingCore.ID, "s2c");
    private static final ResourceLocation C2S = new ResourceLocation(HardcoreQuestingCore.ID, "c2s");
    private static int id = 0;
    private static final Map<Class<? extends IMessage>, Tuple<Class<? extends IMessageHandler>, Integer>> PACKET_HANDLERS = new HashMap<>();
    
    public static void init() {
        registerMessage(OpenGuiMessage.Handler.class, OpenGuiMessage.class, id++, EnvType.CLIENT);
        
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
        
        if (HardcoreQuestingCore.platform.isClient()) {
            HardcoreQuestingCore.platform.getNetworkManager().registerS2CHandler(S2C, (packetContext, packetByteBuf) -> {
                int id = packetByteBuf.readInt();
                for (Map.Entry<Class<? extends IMessage>, Tuple<Class<? extends IMessageHandler>, Integer>> entry : PACKET_HANDLERS.entrySet()) {
                    if (entry.getValue().getB() == id) {
                        try {
                            IMessage message = entry.getKey().newInstance();
                            message.fromBytes(packetByteBuf, packetContext);
                            IMessageHandler<IMessage, ?> handler = entry.getKey() != entry.getValue().getA() ? entry.getValue().getA().newInstance() : (IMessageHandler<IMessage, ?>) message;
                            handler.onMessage(message, packetContext);
                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
                HardcoreQuestingCore.LOGGER.error("Invalid Packet ID: " + id);
            });
        }
        HardcoreQuestingCore.platform.getNetworkManager().registerC2SHandler(C2S, (packetContext, packetByteBuf) -> {
            int id = packetByteBuf.readInt();
            for (Map.Entry<Class<? extends IMessage>, Tuple<Class<? extends IMessageHandler>, Integer>> entry : PACKET_HANDLERS.entrySet()) {
                if (entry.getValue().getB() == id) {
                    try {
                        IMessage message = entry.getKey().newInstance();
                        message.fromBytes(packetByteBuf, packetContext);
                        IMessageHandler<IMessage, ?> handler = entry.getKey() != entry.getValue().getA() ? entry.getValue().getA().newInstance() : (IMessageHandler<IMessage, ?>) message;
                        handler.onMessage(message, packetContext);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            HardcoreQuestingCore.LOGGER.error("Invalid Packet ID: " + id);
        });
    }
    
    private static void registerMessage(Class<? extends IMessageHandler> handlerClass, Class<? extends IMessage> messageClass, int id, EnvType envType) {
        PACKET_HANDLERS.put(messageClass, new Tuple<>(handlerClass, id));
    }
    
    public static void sendToPlayer(IMessage message, ServerPlayer player) {
        if (player == null) return;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        Tuple<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getB());
        message.toBytes(buf);
        HardcoreQuestingCore.platform.getNetworkManager().sendToPlayer(player, S2C, buf);
    }
    
    public static void sendToAllPlayers(IMessage message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        Tuple<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getB());
        message.toBytes(buf);
        for (ServerPlayer entity : HardcoreQuestingCore.getServer().getPlayerList().getPlayers()) {
            HardcoreQuestingCore.platform.getNetworkManager().sendToPlayer(entity, S2C, new FriendlyByteBuf(buf.copy()));
        }
    }
    
    public static void sendToServer(IMessage message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        Tuple<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getB());
        message.toBytes(buf);
        HardcoreQuestingCore.platform.getNetworkManager().sendToServer(C2S, buf);
    }
    
    public static void sendToPlayersAround(IMessage message, BlockEntity te, double radius) {
        BlockPos pos = te.getBlockPos();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        Tuple<Class<? extends IMessageHandler>, Integer> pair = PACKET_HANDLERS.get(message.getClass());
        buf.writeInt(pair.getB());
        message.toBytes(buf);
        Packet<?> packet = HardcoreQuestingCore.platform.getNetworkManager().createToPlayerPacket(S2C, buf);
        HardcoreQuestingCore.getServer().getPlayerList().broadcast(null, pos.getX(), pos.getY(), pos.getZ(), radius, te.getLevel().dimension(), packet);
    }
    
    public static <T extends BlockEntity & IBlockSync> void sendBlockUpdate(T block, Player player, int type) {
        StringWriter data = new StringWriter();
        boolean onServer = !block.getLevel().isClientSide;
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
            if (player instanceof ServerPlayer) {
                sendToPlayer(message, (ServerPlayer) player);
            } else {
                sendToPlayersAround(message, block, IBlockSync.BLOCK_UPDATE_RANGE);
            }
        }
    }
    
    public static void sendSyncPacket(BlockEntity tile) {
        if (tile instanceof ISyncableTile && !tile.getLevel().isClientSide) {
            sendToPlayersAround(new SyncableTileMessage(tile), tile, 128D);
        }
    }
}
