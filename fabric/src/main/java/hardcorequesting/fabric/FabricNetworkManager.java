package hardcorequesting.fabric;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricNetworkManager implements NetworkManager {
    
    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        CustomPacketPayload.Type<GenericPayload> type = new CustomPacketPayload.Type<>(id);
        PayloadTypeRegistry.playS2C().register(type, GenericPayload.codec(type));
        if (HardcoreQuestingCore.platform.isClient())
            registerS2CReceiver(type, consumer);
    }

    @Environment(EnvType.CLIENT)
    private void registerS2CReceiver(CustomPacketPayload.Type<GenericPayload> type, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> consumer.accept(new PacketContext() {
            @Override
            public Player getPlayer() {
                return context.player();
            }

            @Override
            public Consumer<Runnable> getTaskQueue() {
                return context.client()::execute;
            }

            @Override
            public boolean isClient() {
                return true;
            }
        }, payload.buffer));
    }
    
    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {

        CustomPacketPayload.Type<GenericPayload> type = new CustomPacketPayload.Type<>(id);
        PayloadTypeRegistry.playC2S().register(type, GenericPayload.codec(type));
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> consumer.accept(new PacketContext() {
            @Override
            public Player getPlayer() {
                return context.player();
            }

            @Override
            public Consumer<Runnable> getTaskQueue() {
                return context.server()::execute;
            }

            @Override
            public boolean isClient() {
                return false;
            }
        }, payload.buffer));
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void sendToServer(ResourceLocation id, FriendlyByteBuf buf) {
        ClientPlayNetworking.send(new GenericPayload(id, buf));
    }
    
    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, new GenericPayload(id, buf));
    }
    
    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buf) {
        return ServerPlayNetworking.createS2CPacket(new GenericPayload(id, buf));
    }

    public record GenericPayload(Type<GenericPayload> type, FriendlyByteBuf buffer) implements CustomPacketPayload {
        GenericPayload(ResourceLocation id, FriendlyByteBuf buffer) {
            this(new Type<>(id), buffer);
        }

        @Override
        public Type<GenericPayload> type() {
            return this.type;
        }

        private static StreamCodec<FriendlyByteBuf, GenericPayload> codec(Type<GenericPayload> type) {
            return StreamCodec.of((buffer, payload) -> {
                buffer.writeBytes(payload.buffer);
                payload.buffer.resetReaderIndex();
            }, buffer -> {
                FriendlyByteBuf storedBuffer = new FriendlyByteBuf(Unpooled.buffer());
                storedBuffer.writeBytes(buffer);
                return new GenericPayload(type, storedBuffer);
            });
        }
    }
}
