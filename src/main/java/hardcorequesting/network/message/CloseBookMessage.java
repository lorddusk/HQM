package hardcorequesting.network.message;

import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.QuestingData;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class CloseBookMessage implements IMessage {
    
    private UUID playerID;
    
    public CloseBookMessage() {
    }
    
    public CloseBookMessage(UUID playerID) {
        this.playerID = playerID;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.playerID = buf.readUuid();
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(this.playerID);
    }
    
    public static class Handler implements IMessageHandler<CloseBookMessage, IMessage> {
        
        @Override
        public IMessage onMessage(CloseBookMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(CloseBookMessage message, PacketContext ctx) {
            QuestingData.getQuestingData(message.playerID).getTeam().getEntry(message.playerID).setBookOpen(false);
        }
    }
}
