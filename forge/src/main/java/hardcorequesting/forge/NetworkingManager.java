package hardcorequesting.forge;

import com.google.common.collect.Maps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NetworkingManager implements NetworkManager {
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(HardcoreQuestingCore.ID, "network");
    private static final ResourceLocation SYNC_IDS = new ResourceLocation(HardcoreQuestingCore.ID, "sync_ids");
    static final EventNetworkChannel CHANNEL = NetworkRegistry.newEventChannel(CHANNEL_ID, () -> "1", version -> true, version -> true);
    static final Map<ResourceLocation, BiConsumer<PacketContext, PacketBuffer>> S2C = Maps.newHashMap();
    static final Map<ResourceLocation, BiConsumer<PacketContext, PacketBuffer>> C2S = Maps.newHashMap();
    
    public static void init() {
        CHANNEL.addListener(createPacketHandler(NetworkEvent.ClientCustomPayloadEvent.class, C2S));
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientNetworkingManager::initClient);
    }
    
    static <T extends NetworkEvent> Consumer<T> createPacketHandler(Class<T> clazz, Map<ResourceLocation, BiConsumer<PacketContext, PacketBuffer>> map) {
        return event -> {
            if (event.getClass() != clazz) return;
            NetworkEvent.Context context = event.getSource().get();
            if (context.getPacketHandled()) return;
            PacketBuffer buffer = new PacketBuffer(event.getPayload().copy());
            ResourceLocation type = buffer.readResourceLocation();
            BiConsumer<PacketContext, PacketBuffer> consumer = map.get(type);
            
            if (consumer != null) {
                consumer.accept(new PacketContext() {
                    @Override
                    public PlayerEntity getPlayer() {
                        return isClient() ? getClientPlayer() : context.getSender();
                    }
                    
                    @Override
                    public Consumer<Runnable> getTaskQueue() {
                        return context::enqueueWork;
                    }
                    
                    @Override
                    public boolean isClient() {
                        return context.getDirection().getReceptionSide() == LogicalSide.CLIENT;
                    }
                    
                    private PlayerEntity getClientPlayer() {
                        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientNetworkingManager::getClientPlayer);
                    }
                }, buffer);
            }
            context.setPacketHandled(true);
        };
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, PacketBuffer> consumer) {
        S2C.put(id, consumer);
    }
    
    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, PacketBuffer> consumer) {
        C2S.put(id, consumer);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void sendToServer(ResourceLocation id, PacketBuffer buffer) {
        ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
            packetBuffer.writeResourceLocation(id);
            packetBuffer.writeBytes(buffer);
            connection.send(NetworkDirection.PLAY_TO_SERVER.buildPacket(Pair.of(packetBuffer, 0), CHANNEL_ID).getThis());
        }
    }
    
    public void sendToClient(ResourceLocation id, PacketDistributor.PacketTarget target, PacketBuffer buffer) {
        target.send(createToPlayerPacket(id, buffer));
    }
    
    @Override
    public void sendToPlayer(ServerPlayerEntity player, ResourceLocation id, PacketBuffer buffer) {
        sendToClient(id, PacketDistributor.PLAYER.with(() -> player), buffer);
    }
    
    @Override
    public IPacket<?> createToPlayerPacket(ResourceLocation id, PacketBuffer buffer) {
        PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
        packetBuffer.writeResourceLocation(id);
        packetBuffer.writeBytes(buffer);
        return NetworkDirection.PLAY_TO_CLIENT.buildPacket(Pair.of(packetBuffer, 0), CHANNEL_ID).getThis();
    }
}