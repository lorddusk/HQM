package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.ClientChange;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public void fromBytes(ByteBuf buf) {
        this.update = ClientChange.values()[buf.readInt()];
        this.data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        ByteBufUtils.writeUTF8String(buf, data);
    }

    public static class Handler implements IMessageHandler<SoundMessage, IMessage> {

        @Override
        public IMessage onMessage(SoundMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(SoundMessage message, MessageContext ctx) {
            message.update.parse(HardcoreQuesting.proxy.getPlayer(ctx), message.data);
        }
    }
}
