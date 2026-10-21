package hardcorequesting.common.network.message;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.GuiType;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class OpenGuiMessage implements IMessage {
    private String data;
    private GuiType gui;
    
    public OpenGuiMessage() {
        this.gui = GuiType.NONE;
    }
    
    public OpenGuiMessage(GuiType gui, String data) {
        this.gui = gui;
        this.data = data;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        int ordinal = buf.readInt();
        // A gui that does not exist is left out, the handler ignores the message
        this.gui = ordinal >= 0 && ordinal < GuiType.values().length ? GuiType.values()[ordinal] : null;
        if (this.gui == GuiType.NONE) return;
        data = buf.readUtf(32767);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.gui.ordinal());
        if (this.gui == GuiType.NONE) return;
        buf.writeUtf(data);
    }
    
    public static class Handler implements IMessageHandler<OpenGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(OpenGuiMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(OpenGuiMessage message, PacketContext ctx) {
            if (message.gui == null) {
                HardcoreQuestingCore.LOGGER.warn("Ignored an OpenGuiMessage with an unknown gui");
                return;
            }
            message.gui.open(HardcoreQuestingCore.proxy.getPlayer(ctx), message.data);
        }
    }
}
