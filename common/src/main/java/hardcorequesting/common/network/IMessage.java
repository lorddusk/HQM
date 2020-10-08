package hardcorequesting.common.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IMessage {
    void fromBytes(FriendlyByteBuf buf, PacketContext context);
    
    void toBytes(FriendlyByteBuf buf);
}
