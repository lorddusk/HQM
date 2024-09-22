package hardcorequesting.forge;

import com.google.common.collect.Maps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class NetworkingManager implements NetworkManager {
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> S2C = Maps.newHashMap();
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> C2S = Maps.newHashMap();

    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(HardcoreQuestingCore.ID)
                .versioned("1");
        for (Map.Entry<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> entry : S2C.entrySet()) {
            var id = entry.getKey();
            var handler = entry.getValue();
            CustomPacketPayload.Type<GenericPayload> type = new CustomPacketPayload.Type<>(id);
            registrar.playToClient(type, GenericPayload.codec(type), (payload, context) -> handler.accept(new PacketContext() {

                @Override
                public Player getPlayer() {
                    return context.player();
                }

                @Override
                public Consumer<Runnable> getTaskQueue() {
                    return context::enqueueWork;
                }

                @Override
                public boolean isClient() {
                    return true;
                }
            }, payload.buffer()));
        }
        for (Map.Entry<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> entry : C2S.entrySet()) {
            var id = entry.getKey();
            var handler = entry.getValue();
            CustomPacketPayload.Type<GenericPayload> type = new CustomPacketPayload.Type<>(id);
            registrar.playToServer(type, GenericPayload.codec(type), (payload, context) -> handler.accept(new PacketContext() {

                @Override
                public Player getPlayer() {
                    return context.player();
                }

                @Override
                public Consumer<Runnable> getTaskQueue() {
                    return context::enqueueWork;
                }

                @Override
                public boolean isClient() {
                    return false;
                }
            }, payload.buffer()));
        }
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

    @Override
    public void registerS2CHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        S2C.put(id, consumer);
    }
    
    @Override
    public void registerC2SHandler(ResourceLocation id, BiConsumer<PacketContext, FriendlyByteBuf> consumer) {
        C2S.put(id, consumer);
    }

    @Override
    public void sendToServer(ResourceLocation id, FriendlyByteBuf buffer) {
        PacketDistributor.sendToServer(new GenericPayload(id, buffer));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buffer) {
        PacketDistributor.sendToPlayer(player, new GenericPayload(id, buffer));
    }
    
    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buffer) {
        return new ClientboundCustomPayloadPacket(new GenericPayload(id, buffer));
    }
}
