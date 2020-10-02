package hardcorequesting.common.network.message;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.util.OPBookHelper;
import net.minecraft.network.FriendlyByteBuf;

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
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.action = OPBookHelper.OpAction.values()[buf.readInt()];
        this.data = buf.readUtf(32767);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.action.ordinal());
        buf.writeUtf(data);
    }
    
    public static class Handler implements IMessageHandler<OpActionMessage, IMessage> {
        
        @Override
        public IMessage onMessage(OpActionMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(OpActionMessage message, PacketContext ctx) {
            message.action.process(HardcoreQuestingCore.proxy.getPlayer(ctx), message.data);
        }
    }
}
