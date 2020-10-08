package hardcorequesting.common.network.message;

import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.team.TeamError;
import net.minecraft.network.FriendlyByteBuf;

public class TeamErrorMessage implements IMessage {
    
    TeamError error;
    
    public TeamErrorMessage() {
    }
    
    public TeamErrorMessage(TeamError error) {
        this.error = error;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.error = TeamError.values()[buf.readInt()];
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.error.ordinal());
    }
    
    public static class Handler implements IMessageHandler<TeamErrorMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamErrorMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamErrorMessage message, PacketContext ctx) {
            TeamError.latestError = message.error;
        }
    }
}
