package hardcorequesting.forge;

import com.google.common.collect.Maps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.event.EventNetworkChannel;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NetworkingManager implements NetworkManager {
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(HardcoreQuestingCore.ID, "network");
    private static final ResourceLocation SYNC_IDS = new ResourceLocation(HardcoreQuestingCore.ID, "sync_ids");
    static final EventNetworkChannel CHANNEL = NetworkRegistry.newEventChannel(CHANNEL_ID, () -> "1", version -> true, version -> true);
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> S2C = Maps.newHashMap();
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> C2S = Maps.newHashMap();
    
    public static void init() {
        CHANNEL.addListener(createPacketHandler(NetworkEvent.ClientCustomPayloadEvent.class, C2S));

        if(FMLEnvironment.dist.isClient()){
            ClientNetworkingManager.initClient();
        }
    }
    
    static <T extends NetworkEvent> Consumer<T> createPacketHandler(Class<T> clazz, Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> map) {
        return event -> {
            if (event.getClass() != clazz) return;
            NetworkEvent.Context context = event.getSource();
            if (context.getPacketHandled()) return;
            FriendlyByteBuf buffer = new FriendlyByteBuf(event.getPayload().copy());
            ResourceLocation type = buffer.readResourceLocation();
            BiConsumer<PacketContext, FriendlyByteBuf> consumer = map.get(type);
            
            if (consumer != null) {
                consumer.accept(new PacketContext() {
                    @Override
                    public Player getPlayer() {
                        return this.isClient() ? this.getClientPlayer() : context.getSender();
                    }
                    
                    @Override
                    public Consumer<Runnable> getTaskQueue() {
                        return context::enqueueWork;
                    }
                    
                    @Override
                    public boolean isClient() {
                        return context.getDirection().getReceptionSide() == LogicalSide.CLIENT;
                    }

                    @OnlyIn(Dist.CLIENT)
                    private Player getClientPlayer() {
                        return FMLEnvironment.dist.isClient() ? ClientNetworkingManager.getClientPlayer() : null;
                    }
                }, buffer);
            }
            context.setPacketHandled(true);
        };
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        S2C.put(id, consumer);
    }
    
    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        C2S.put(id, consumer);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void sendToServer(ResourceLocation id, FriendlyByteBuf buffer) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
            packetBuffer.writeResourceLocation(id);
            packetBuffer.writeBytes(buffer);
            connection.send(PlayNetworkDirection.PLAY_TO_SERVER.buildPacket(new INetworkDirection.PacketData(packetBuffer, 0), CHANNEL_ID));
        }
    }
    
    public void sendToClient(ResourceLocation id, PacketDistributor.PacketTarget target, FriendlyByteBuf buffer) {
        target.send(createToPlayerPacket(id, buffer));
    }
    
    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buffer) {
        sendToClient(id, PacketDistributor.PLAYER.with(() -> player), buffer);
    }
    
    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buffer) {
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(Unpooled.buffer());
        packetBuffer.writeResourceLocation(id);
        packetBuffer.writeBytes(buffer);
        return PlayNetworkDirection.PLAY_TO_CLIENT.buildPacket(new INetworkDirection.PacketData(packetBuffer, 0), CHANNEL_ID);
    }
}