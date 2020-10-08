package hardcorequesting.common.network.message;

import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class CloseBookMessage implements IMessage {
    
    private UUID playerID;
    
    public CloseBookMessage() {
    }
    
    public CloseBookMessage(UUID playerID) {
        this.playerID = playerID;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.playerID = buf.readUUID();
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerID);
    }
    
    public static class Handler implements IMessageHandler<CloseBookMessage, IMessage> {
        
        @Override
        public IMessage onMessage(CloseBookMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(CloseBookMessage message, PacketContext ctx) {
            QuestingDataManager.getInstance().getQuestingData(message.playerID).getTeam().getEntry(message.playerID).setBookOpen(false);
        }
    }
}
