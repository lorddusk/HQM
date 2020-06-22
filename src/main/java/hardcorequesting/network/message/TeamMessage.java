package hardcorequesting.network.message;

import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.team.TeamAction;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

public class TeamMessage implements IMessage {
    
    private TeamAction action;
    private String data; // todo rewrite to use CompoundTag instead of a freaking string!
    
    public TeamMessage() {
    }
    
    public TeamMessage(TeamAction action, String data) {
        this.action = action;
        this.data = data;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.action = TeamAction.values()[buf.readInt()];
        this.data = buf.readString(32767);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(action.ordinal());
        buf.writeString(data);
    }
    
    public static class Handler implements IMessageHandler<TeamMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamMessage message, PacketContext ctx) {
            message.action.process(ctx.getPlayer(), message.data);
        }
    }
}
