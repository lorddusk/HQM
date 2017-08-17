package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.util.OPBookHelper;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public void fromBytes(ByteBuf buf) {
        this.action = OPBookHelper.OpAction.values()[buf.readInt()];
        this.data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action.ordinal());
        ByteBufUtils.writeUTF8String(buf, data);
    }

    public static class Handler implements IMessageHandler<OpActionMessage, IMessage> {

        @Override
        public IMessage onMessage(OpActionMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(OpActionMessage message, MessageContext ctx) {
            message.action.process(HardcoreQuesting.proxy.getPlayer(ctx), message.data);
        }
    }
}
