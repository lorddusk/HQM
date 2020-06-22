package hardcorequesting.network.message;

import hardcorequesting.client.ClientChange;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

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
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.update = ClientChange.values()[buf.readInt()];
        data = buf.readString(32767);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        buf.writeString(data);
    }
    
    public static class Handler implements IMessageHandler<ClientUpdateMessage, IMessage> {
        
        @Override
        public IMessage onMessage(ClientUpdateMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(ClientUpdateMessage message, PacketContext ctx) {
            message.update.parse(ctx.getPlayer(), message.data);
        }
    }
}
