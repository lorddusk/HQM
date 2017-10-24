package hardcorequesting.network.message;

import hardcorequesting.client.ClientChange;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public void fromBytes(ByteBuf buf) {
        this.update = ClientChange.values()[buf.readInt()];
        data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        ByteBufUtils.writeUTF8String(buf, data);
    }

    public static class Handler implements IMessageHandler<ClientUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(ClientUpdateMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(ClientUpdateMessage message, MessageContext ctx) {
            message.update.parse(ctx.getServerHandler().player, message.data);
        }
    }
}
