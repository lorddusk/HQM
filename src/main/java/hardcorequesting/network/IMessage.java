package hardcorequesting.network;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public interface IMessage {
    void fromBytes(FriendlyByteBuf buf, PacketContext context);
    
    void toBytes(FriendlyByteBuf buf);
}
