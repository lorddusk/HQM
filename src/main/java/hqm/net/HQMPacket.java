package hqm.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * @author canitzp
 */
// todo packet update to 1.13
public class HQMPacket {

    /*private CompoundNBT data;
    private int action;

    private PlayerEntity player;

    public HQMPacket(){}

    public HQMPacket(NetActions action, PlayerEntity sender, CompoundNBT data){
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
        CompoundNBT data = message.data;
        if(action != null && data != null && !data.hasNoTags()){
            PlayerEntity sender = ctx.getServerHandler().player.world.getPlayerEntityByUUID(data.getUniqueId("PlayerId"));
            PlayerEntity receiver = ctx.getServerHandler().player;
            action.action(data, sender, receiver);
        }
        return null;
    }*/

}
