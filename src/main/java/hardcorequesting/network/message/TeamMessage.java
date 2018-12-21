package hardcorequesting.network.message;

import hardcorequesting.team.TeamAction;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeamMessage implements IMessage {

    private TeamAction action;
    private String data; // todo rewrite to use NBTTagCompound instead of a freaking string!

    public TeamMessage() {
    }

    public TeamMessage(TeamAction action, String data) {
        this.action = action;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = TeamAction.values()[buf.readInt()];
        this.data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        ByteBufUtils.writeUTF8String(buf, data);
    }

    public static class Handler implements IMessageHandler<TeamMessage, IMessage> {

        @Override
        public IMessage onMessage(TeamMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(TeamMessage message, MessageContext ctx) {
            message.action.process(ctx.getServerHandler().player, message.data);
        }
    }
}
