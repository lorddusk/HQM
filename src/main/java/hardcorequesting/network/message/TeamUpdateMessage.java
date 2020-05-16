package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.TeamUpdateType;
import hardcorequesting.util.SyncUtil;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.PacketByteBuf;

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
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.type = TeamUpdateType.values()[buf.readInt()];
        this.data = SyncUtil.readLargeString(buf);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.type.ordinal());
        
        SyncUtil.writeLargeString(this.data, buf);
    }
    
    public static class Handler implements IMessageHandler<TeamUpdateMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamUpdateMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamUpdateMessage message, PacketContext ctx) {
            message.type.update(QuestingData.getQuestingData(HardcoreQuesting.proxy.getPlayer(ctx)).getTeam(), message.data);
        }
    }
}
