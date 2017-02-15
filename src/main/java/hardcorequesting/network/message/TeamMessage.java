package hardcorequesting.network.message;

import hardcorequesting.team.TeamAction;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeamMessage implements IMessage {

    private TeamAction action;
    private String data;

    public TeamMessage() {
    }

    public TeamMessage(TeamAction action, String data) {
        this.action = action;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = TeamAction.values()[buf.readInt()];
        int size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        buf.writeInt(data.getBytes().length);
        buf.writeBytes(data.getBytes());
    }

    public static class Handler implements IMessageHandler<TeamMessage, IMessage> {

        @Override
        public IMessage onMessage(TeamMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(TeamMessage message, MessageContext ctx) {
            message.action.process(ctx.getServerHandler().playerEntity, message.data);
        }
    }
}
