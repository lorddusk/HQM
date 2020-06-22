package hardcorequesting.network.message;

import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.QuestingData;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class LivesUpdate implements IMessage {
    
    private UUID uuid;
    private int lives;
    
    public LivesUpdate() {
    }
    
    public LivesUpdate(UUID uuid, int lives) {
        this.uuid = uuid;
        this.lives = lives;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.lives = buf.readInt();
        uuid = buf.readUuid();
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.lives);
        buf.writeUuid(this.uuid);
    }
    
    public static class Handler implements IMessageHandler<LivesUpdate, IMessage> {
        @Override
        public IMessage onMessage(LivesUpdate message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(LivesUpdate message, PacketContext ctx) {
            QuestingData.getQuestingData(message.uuid).setRawLives(message.lives);
        }
    }
}
