package hardcorequesting.fabric;

import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricNetworkManager implements NetworkManager {
    
    @Environment(EnvType.CLIENT)
    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        ClientPlayNetworking.registerGlobalReceiver(id,
                (client, handler, buf, sender) -> consumer.accept(new PacketContext() {
                    @Override
                    public Player getPlayer() {
                        return client.player;
                    }
    
                    @Override
                    public Consumer<Runnable> getTaskQueue() {
                        return client::execute;
                    }
    
                    @Override
                    public boolean isClient() {
                        return true;
                    }
                }, buf));
    }
    
    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        ServerPlayNetworking.registerGlobalReceiver(id,
                (server, player, handler, buf, sender) -> consumer.accept(new PacketContext() {
                    @Override
                    public Player getPlayer() {
                        return player;
                    }
    
                    @Override
                    public Consumer<Runnable> getTaskQueue() {
                        return server::execute;
                    }
    
                    @Override
                    public boolean isClient() {
                        return false;
                    }
                }, buf));
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
        ClientPlayNetworking.send(id, buf);
    }
    
    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, id, buf);
    }
    
    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buf) {
        return ServerPlayNetworking.createS2CPacket(id, buf);
    }
}