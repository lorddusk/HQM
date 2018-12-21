package hardcorequesting.network.message;

import hardcorequesting.network.ISyncableTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SyncableTileMessage implements IMessage, IMessageHandler<SyncableTileMessage, IMessage>{
    
    private TileEntity tileToSync;
    private NBTTagCompound data;
    
    public SyncableTileMessage(){}
    
    public SyncableTileMessage(TileEntity tileToSync){
        this.tileToSync = tileToSync;
        if(tileToSync instanceof ISyncableTile){
            this.data = ((ISyncableTile) tileToSync).getSyncData();
        }
    }
    
    @Override
    public void fromBytes(ByteBuf buf){
        int worldId = buf.readInt();
        BlockPos pos = new PacketBuffer(buf).readBlockPos();
        this.data = ByteBufUtils.readTag(buf);
    
        World world = DimensionManager.getWorld(worldId);
        if(world != null){
            this.tileToSync = world.getTileEntity(pos);
            System.out.println(this.tileToSync.hashCode());
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.tileToSync.getWorld().provider.getDimension());
        new PacketBuffer(buf).writeBlockPos(this.tileToSync.getPos());
        ByteBufUtils.writeTag(buf, this.data);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(SyncableTileMessage message, MessageContext ctx){
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if(message.tileToSync instanceof ISyncableTile && message.data != null){
                System.out.println(message.tileToSync.getWorld().getTileEntity(message.tileToSync.getPos()).hashCode());
                ((ISyncableTile) message.tileToSync).onData(message.data);
                System.out.println(message.tileToSync.hashCode());
            }
        });
        return null;
    }
}
