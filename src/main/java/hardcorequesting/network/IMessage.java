package hardcorequesting.network;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

public interface IMessage {
    void fromBytes(PacketByteBuf buf, PacketContext context);
    
    void toBytes(PacketByteBuf buf);
}
