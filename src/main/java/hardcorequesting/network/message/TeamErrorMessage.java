package hardcorequesting.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import hardcorequesting.team.TeamError;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class TeamErrorMessage implements IMessage {
    TeamError error;

    public TeamErrorMessage() {
    }

    public TeamErrorMessage(TeamError error) {
        this.error = error;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.error = TeamError.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.error.ordinal());
    }

    public static class Handler implements IMessageHandler<TeamErrorMessage, IMessage> {
        @Override
        public IMessage onMessage(TeamErrorMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(TeamErrorMessage message, MessageContext ctx) {
            TeamError.latestError = message.error;
        }
    }
}
