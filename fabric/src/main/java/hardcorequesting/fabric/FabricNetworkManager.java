package hardcorequesting.fabric;

import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricNetworkManager implements NetworkManager {
    private PacketContext fromFabric(net.fabricmc.fabric.api.network.PacketContext context) {
        return new PacketContext() {
            @Override
            public Player getPlayer() {
                return context.getPlayer();
            }
            
            @Override
            public Consumer<Runnable> getTaskQueue() {
                return runnable -> context.getTaskQueue().execute(runnable);
            }
            
            @Override
            public boolean isClient() {
                return context.getPacketEnvironment() == EnvType.CLIENT;
            }
        };
    }
    
    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        ClientSidePacketRegistry.INSTANCE.register(id, (packetContext, friendlyByteBuf) -> consumer.accept(fromFabric(packetContext), friendlyByteBuf));
    }
    
    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        ServerSidePacketRegistry.INSTANCE.register(id, (packetContext, friendlyByteBuf) -> consumer.accept(fromFabric(packetContext), friendlyByteBuf));
    }
    
    @Override
    public void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
        ClientSidePacketRegistry.INSTANCE.sendToServer(id, buf);
    }
    
    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, id, buf);
    }
    
    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buf) {
        return ServerSidePacketRegistry.INSTANCE.toPacket(id, buf);
    }
}
