package hardcorequesting.common.network.message;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.TeamUpdateType;
import hardcorequesting.common.util.SyncUtil;
import net.minecraft.network.FriendlyByteBuf;

public class TeamUpdateMessage implements IMessage {
    
    private TeamUpdateType type;
    private String data;
    
    public TeamUpdateMessage() {
    }
    
    public TeamUpdateMessage(TeamUpdateType type, String data) {
        this.type = type;
        this.data = data;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        int ordinal = buf.readInt();
        // A type that does not exist is left out, the handler ignores the message
        this.type = ordinal >= 0 && ordinal < TeamUpdateType.values().length ? TeamUpdateType.values()[ordinal] : null;
        this.data = SyncUtil.readLargeString(buf);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.type.ordinal());
        
        SyncUtil.writeLargeString(this.data, buf);
    }
    
    public static class Handler implements IMessageHandler<TeamUpdateMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamUpdateMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamUpdateMessage message, PacketContext ctx) {
            if (message.type == null) {
                HardcoreQuestingCore.LOGGER.warn("Ignored a TeamUpdateMessage with an unknown type");
                return;
            }
            message.type.update(QuestingDataManager.getInstance().getQuestingData(HardcoreQuestingCore.proxy.getPlayer(ctx)).getTeam(), message.data);
        }
    }
}
