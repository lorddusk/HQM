package hardcorequesting.common.network.message;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

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
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.update = ClientChange.values()[buf.readInt()];
        this.data = buf.readUtf(32767);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        buf.writeUtf(data);
    }
    
    public static class Handler implements IMessageHandler<SoundMessage, IMessage> {
        @Override
        public IMessage onMessage(SoundMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(SoundMessage message, PacketContext ctx) {
            message.update.parse(HardcoreQuestingCore.proxy.getPlayer(ctx), message.data);
        }
    }
}
