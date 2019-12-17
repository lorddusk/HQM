package hqm.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author canitzp
 */
public class HQMPacket {

    private CompoundNBT data;
    private int action;

    private PlayerEntity player;

    public HQMPacket(){}
    
    public HQMPacket(PacketBuffer buf){
        this.action = buf.readInt();
        this.data = buf.readCompoundTag();
    }

    public HQMPacket(NetActions action, PlayerEntity sender, CompoundNBT data){
        this.action = action.ordinal();
        this.data = data;
        this.data.putUniqueId("PlayerId", sender.getUniqueID());
        this.player = sender;
    }

    public HQMPacket execute(){
        NetActions.get(this.action).action(this.data, this.player, this.player);
        return this;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.action);
        buf.writeCompoundTag(this.data);
    }
    
    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            NetActions action = NetActions.get(this.action);
            if(action != null && this.data != null && !this.data.isEmpty()){
                PlayerEntity sender = ctx.getSender().getEntityWorld().getPlayerByUuid(this.data.getUniqueId("PlayerId"));
                PlayerEntity receiver = ctx.getSender();
                action.action(this.data, sender, receiver);
            }
        });
        ctx.setPacketHandled(true);
    }

}
