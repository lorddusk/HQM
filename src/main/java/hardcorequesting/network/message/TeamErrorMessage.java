package hardcorequesting.network.message;

import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.team.TeamError;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.PacketByteBuf;

public class TeamErrorMessage implements IMessage {
    
    TeamError error;
    
    public TeamErrorMessage() {
    }
    
    public TeamErrorMessage(TeamError error) {
        this.error = error;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.error = TeamError.values()[buf.readInt()];
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.error.ordinal());
    }
    
    public static class Handler implements IMessageHandler<TeamErrorMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamErrorMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamErrorMessage message, PacketContext ctx) {
            TeamError.latestError = message.error;
        }
    }
}
