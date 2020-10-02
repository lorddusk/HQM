package hardcorequesting.common.platform;

import hardcorequesting.common.network.PacketContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public interface NetworkManager {
    @Environment(EnvType.CLIENT)
    void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer);
    
    void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer);
    
    @Environment(EnvType.CLIENT)
    void sendToServer(ResourceLocation id, FriendlyByteBuf buf);
    
    void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf);
    
    Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buf);
}
