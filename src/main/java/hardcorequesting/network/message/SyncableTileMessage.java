package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.network.ISyncableTile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        Identifier worldId = buf.readIdentifier();
        BlockPos pos = buf.readBlockPos();
        this.data = buf.readCompoundTag();
        
        World world = HardcoreQuesting.getServer().getWorld(Registry.DIMENSION_TYPE.get(worldId));
        if (world != null) {
            this.tileToSync = world.getBlockEntity(pos);
            System.out.println(this.tileToSync.hashCode());
        }
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeIdentifier(Registry.DIMENSION_TYPE.getId(this.tileToSync.getWorld().getDimension().getType()));
        buf.writeBlockPos(this.tileToSync.getPos());
        buf.writeCompoundTag(this.data);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public IMessage onMessage(SyncableTileMessage message, PacketContext ctx) {
        ctx.getTaskQueue().execute(() -> {
            if (message.tileToSync instanceof ISyncableTile && message.data != null) {
                System.out.println(message.tileToSync.getWorld().getBlockEntity(message.tileToSync.getPos()).hashCode());
                ((ISyncableTile) message.tileToSync).onData(message.data);
                System.out.println(message.tileToSync.hashCode());
            }
        });
        return null;
    }
}
