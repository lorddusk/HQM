package hardcorequesting.network.message;

import hardcorequesting.client.ClientChange;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
        int size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.update.ordinal());
        buf.writeInt(this.data.getBytes().length);
        buf.writeBytes(this.data.getBytes());
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
