package hardcorequesting.common.network.message;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class ClientUpdateMessage implements IMessage {
    
    private ClientChange update;
    private String data;
    
    public ClientUpdateMessage() {
    }
    
    public ClientUpdateMessage(ClientChange update, String data) {
        this.update = update;
        this.data = data;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        // Null for a value that does not exist, the handler ignores the message
        this.update = ClientChange.byOrdinal(buf.readInt());
        data = buf.readUtf(32767);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        buf.writeUtf(data);
    }
    
    public static class Handler implements IMessageHandler<ClientUpdateMessage, IMessage> {
        
        @Override
        public IMessage onMessage(ClientUpdateMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(ClientUpdateMessage message, PacketContext ctx) {
            if (message.update == null) {
                HardcoreQuestingCore.LOGGER.warn("Ignored a ClientUpdateMessage with an unknown action from %s", ctx.getPlayer().getScoreboardName());
                return;
            }
            message.update.parse(ctx.getPlayer(), message.data);
        }
    }
}
