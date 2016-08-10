package hardcorequesting.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.GuiType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

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
    public void fromBytes(ByteBuf buf) {
        this.gui = GuiType.values()[buf.readInt()];
        if (this.gui == GuiType.NONE) return;
        int size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.gui.ordinal());
        if (this.gui == GuiType.NONE) return;
        buf.writeInt(this.data.getBytes().length);
        buf.writeBytes(this.data.getBytes());
    }

    public static class Handler implements IMessageHandler<OpenGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(OpenGuiMessage message, MessageContext ctx) {
            message.gui.open(HardcoreQuesting.proxy.getPlayer(ctx), message.data);
        }
    }
}
