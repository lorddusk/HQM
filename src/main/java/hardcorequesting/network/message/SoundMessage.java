package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.ClientChange;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

public class SoundMessage implements IMessage {
    
    private ClientChange update;
    private String data;
    
    public SoundMessage() {
    }
    
    public SoundMessage(ClientChange update, String data) {
        this.update = update;
        this.data = data;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.update = ClientChange.values()[buf.readInt()];
        this.data = buf.readString(32767);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        buf.writeString(data);
    }
    
    public static class Handler implements IMessageHandler<SoundMessage, IMessage> {
        @Override
        public IMessage onMessage(SoundMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(SoundMessage message, PacketContext ctx) {
            message.update.parse(HardcoreQuesting.proxy.getPlayer(ctx), message.data);
        }
    }
}
