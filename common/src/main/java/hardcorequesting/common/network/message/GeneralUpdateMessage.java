package hardcorequesting.common.network.message;

import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class GeneralUpdateMessage implements IMessage, IMessageHandler<GeneralUpdateMessage, IMessage> {
    
    private Player player;
    private CompoundTag data;
    private int usage = -1;
    
    public GeneralUpdateMessage() {}
    
    public GeneralUpdateMessage(Player player, CompoundTag data, int usage) {
        this.player = player;
        this.data = data;
        this.usage = usage;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        UUID playerId = buf.readUUID();
        this.data = buf.readNbt();
        this.usage = buf.readInt();
        
        this.player = context.getPlayer();
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player.getUUID());
        buf.writeNbt(this.data);
        buf.writeInt(this.usage);
    }
    
    @Override
    public IMessage onMessage(GeneralUpdateMessage message, PacketContext ctx) {
        ctx.getTaskQueue().accept(() -> {
            if (message.data != null && message.usage >= 0) {
                GeneralUsage usage = GeneralUsage.values()[message.usage];
                if (message.player != null) {
                    usage.receiveData(message.player, message.data);
                }
            }
        });
        return null;
    }
}
