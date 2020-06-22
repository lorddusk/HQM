package hardcorequesting.network.message;

import hardcorequesting.network.GeneralUsage;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class GeneralUpdateMessage implements IMessage, IMessageHandler<GeneralUpdateMessage, IMessage> {
    
    private PlayerEntity player;
    private CompoundTag data;
    private int usage = -1;
    
    public GeneralUpdateMessage() {}
    
    public GeneralUpdateMessage(PlayerEntity player, CompoundTag data, int usage) {
        this.player = player;
        this.data = data;
        this.usage = usage;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        UUID playerId = buf.readUuid();
        this.data = buf.readCompoundTag();
        this.usage = buf.readInt();
        
        this.player = context.getPlayer();
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(this.player.getUuid());
        buf.writeCompoundTag(this.data);
        buf.writeInt(this.usage);
    }
    
    @Override
    public IMessage onMessage(GeneralUpdateMessage message, PacketContext ctx) {
        if (message.data != null && message.usage >= 0) {
            GeneralUsage usage = GeneralUsage.values()[message.usage];
            if (message.player != null) {
                usage.receiveData(message.player, message.data);
            }
        }
        return null;
    }
}
