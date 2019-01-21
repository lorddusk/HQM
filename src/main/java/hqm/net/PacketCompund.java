package hqm.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCompund implements IMessage, IMessageHandler<PacketCompund, IMessage>{
    
    private int id;
    private NBTTagCompound data;
    
    public PacketCompund(){
    }
    
    public PacketCompund(int id, NBTTagCompound data){
        this.id = id;
        this.data = data;
    }
    
    @Override
    public void fromBytes(ByteBuf buf){
        this.id = buf.readInt();
        this.data = ByteBufUtils.readTag(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.id);
        ByteBufUtils.writeTag(buf, this.data);
    }
    
    @Override
    public IMessage onMessage(PacketCompund message, MessageContext ctx){
        if(message.id != 0 && message.data != null){
            NetManager.onNBTPacket(message.id, message.data);
        }
        return null;
    }
}
