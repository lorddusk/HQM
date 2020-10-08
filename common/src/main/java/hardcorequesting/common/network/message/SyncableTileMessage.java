package hardcorequesting.common.network.message;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.ISyncableTile;
import hardcorequesting.common.network.PacketContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SyncableTileMessage implements IMessage, IMessageHandler<SyncableTileMessage, IMessage> {
    
    private BlockEntity tileToSync;
    private CompoundTag data;
    
    public SyncableTileMessage() {}
    
    public SyncableTileMessage(BlockEntity tileToSync) {
        this.tileToSync = tileToSync;
        if (tileToSync instanceof ISyncableTile) {
            this.data = ((ISyncableTile) tileToSync).getSyncData();
        }
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        ResourceLocation worldId = buf.readResourceLocation();
        BlockPos pos = buf.readBlockPos();
        this.data = buf.readNbt();
        
        Level world = HardcoreQuestingCore.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, worldId));
        if (world != null) {
            this.tileToSync = world.getBlockEntity(pos);
            System.out.println(this.tileToSync.hashCode());
        }
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(this.tileToSync.getLevel().dimension().location());
        buf.writeBlockPos(this.tileToSync.getBlockPos());
        buf.writeNbt(this.data);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public IMessage onMessage(SyncableTileMessage message, PacketContext ctx) {
        ctx.getTaskQueue().accept(() -> {
            if (message.tileToSync instanceof ISyncableTile && message.data != null) {
                System.out.println(message.tileToSync.getLevel().getBlockEntity(message.tileToSync.getBlockPos()).hashCode());
                ((ISyncableTile) message.tileToSync).onData(message.data);
                System.out.println(message.tileToSync.hashCode());
            }
        });
        return null;
    }
}
