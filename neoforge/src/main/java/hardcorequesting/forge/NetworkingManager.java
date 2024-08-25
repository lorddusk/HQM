package hardcorequesting.forge;

import com.google.common.collect.Maps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.platform.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class NetworkingManager implements NetworkManager {
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> S2C = Maps.newHashMap();
    static final Map<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> C2S = Maps.newHashMap();

    static void register(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(HardcoreQuestingCore.ID)
                .versioned("1");
        for (Map.Entry<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> entry : S2C.entrySet()) {
            var id = entry.getKey();
            var handler = entry.getValue();
            registrar.play(id, GenericPayload.reader(id), builder -> builder.client((payload, context) -> handler.accept(new PacketContext() {

                @Override
                public Player getPlayer() {
                    return ClientNetworkingManager.getClientPlayer();
                }

                @Override
                public Consumer<Runnable> getTaskQueue() {
                    return context.workHandler()::execute;
                }

                @Override
                public boolean isClient() {
                    return true;
                }
            }, payload.buffer())));
        }
        for (Map.Entry<ResourceLocation, BiConsumer<PacketContext, FriendlyByteBuf>> entry : C2S.entrySet()) {
            var id = entry.getKey();
            var handler = entry.getValue();
            registrar.play(id, GenericPayload.reader(id), builder -> builder.server((payload, context) -> handler.accept(new PacketContext() {

                @Override
                public Player getPlayer() {

                    return context.player().orElseThrow();
                }

                @Override
                public Consumer<Runnable> getTaskQueue() {

                    return context.workHandler()::execute;
                }

                @Override
                public boolean isClient() {

                    return false;
                }
            }, payload.buffer())));
        }
    }

    public record GenericPayload(ResourceLocation id, FriendlyByteBuf buffer) implements CustomPacketPayload {
        @Override
        public ResourceLocation id() {
            return this.id;
        }

        public static FriendlyByteBuf.Reader<GenericPayload> reader(ResourceLocation id) {
            return buffer -> {
                FriendlyByteBuf storedBuffer = new FriendlyByteBuf(Unpooled.buffer());
                storedBuffer.writeBytes(buffer);
                return new GenericPayload(id, storedBuffer);
            };
        }

        @Override
        public void write(FriendlyByteBuf buffer) {
            buffer.writeBytes(this.buffer);
            this.buffer.resetReaderIndex();
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
        PacketDistributor.SERVER.noArg().send(new GenericPayload(id, buffer));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ResourceLocation id, FriendlyByteBuf buffer) {

        PacketDistributor.PLAYER.with(player).send(new GenericPayload(id, buffer));
    }
    
    @Override
    public Packet<?> createToPlayerPacket(ResourceLocation id, FriendlyByteBuf buffer) {
        return new ClientboundCustomPayloadPacket(new GenericPayload(id, buffer));
    }
}
