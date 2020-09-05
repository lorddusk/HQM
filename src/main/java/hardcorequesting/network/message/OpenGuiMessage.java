package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import net.fabricmc.fabric.api.network.PacketContext;
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
        this.gui = GuiType.values()[buf.readInt()];
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
            ctx.getTaskQueue().submit(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(OpenGuiMessage message, PacketContext ctx) {
            message.gui.open(HardcoreQuesting.proxy.getPlayer(ctx), message.data);
        }
    }
}
