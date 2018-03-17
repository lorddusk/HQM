package hqm.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author canitzp
 */
public class HQMPacket implements IMessage, IMessageHandler<HQMPacket, IMessage> {

    private NBTTagCompound data;
    private int action;

    private EntityPlayer player;

    public HQMPacket(){}

    public HQMPacket(NetActions action, EntityPlayer sender, NBTTagCompound data){
        this.action = action.ordinal();
        this.data = data;
        this.data.setUniqueId("PlayerId", sender.getUniqueID());
        this.player = sender;
    }

    public HQMPacket execute(){
        NetActions.get(this.action).action(this.data, this.player, this.player);
        return this;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = buf.readInt();
        this.data = ByteBufUtils.readTag(buf);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action);
        ByteBufUtils.writeTag(buf, this.data);
    }

    @Override
    public IMessage onMessage(HQMPacket message, MessageContext ctx) {
        NetActions action = NetActions.get(message.action);
        NBTTagCompound data = message.data;
        if(action != null && data != null && !data.hasNoTags()){
            EntityPlayer sender = ctx.getServerHandler().player.world.getPlayerEntityByUUID(data.getUniqueId("PlayerId"));
            EntityPlayer receiver = ctx.getServerHandler().player;
            action.action(data, sender, receiver);
        }
        return null;
    }

}
