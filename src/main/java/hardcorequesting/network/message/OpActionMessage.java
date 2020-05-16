package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.util.OPBookHelper;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.PacketByteBuf;

public class OpActionMessage implements IMessage {
    
    private OPBookHelper.OpAction action;
    private String data;
    
    public OpActionMessage() {
    }
    
    public OpActionMessage(OPBookHelper.OpAction action, String data) {
        this.action = action;
        this.data = data;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.action = OPBookHelper.OpAction.values()[buf.readInt()];
        this.data = buf.readString(32767);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.action.ordinal());
        buf.writeString(data);
    }
    
    public static class Handler implements IMessageHandler<OpActionMessage, IMessage> {
        
        @Override
        public IMessage onMessage(OpActionMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(OpActionMessage message, PacketContext ctx) {
            message.action.process(HardcoreQuesting.proxy.getPlayer(ctx), message.data);
        }
    }
}
