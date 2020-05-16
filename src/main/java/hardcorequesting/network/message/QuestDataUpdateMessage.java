package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.io.adapter.QuestDataAdapter;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class QuestDataUpdateMessage implements IMessage {
    
    private UUID questId;
    private String data;
    private int players;
    
    public QuestDataUpdateMessage() {
    }
    
    public QuestDataUpdateMessage(UUID questId, int players, QuestData data) {
        this.questId = questId;
        this.data = QuestDataAdapter.QUEST_DATA_ADAPTER.toJson(data);
        this.players = players;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.players = buf.readInt();
        this.questId = buf.readUuid();
        int charLength = buf.readInt();
        this.data = buf.readCharSequence(charLength, StandardCharsets.UTF_8).toString();
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeInt(this.players);
        buf.writeUuid(this.questId);
        buf.writeInt(this.data.length());
        buf.writeCharSequence(this.data, StandardCharsets.UTF_8);
    }
    
    public static class Handler implements IMessageHandler<QuestDataUpdateMessage, IMessage> {
        
        @Override
        public IMessage onMessage(QuestDataUpdateMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(QuestDataUpdateMessage message, PacketContext ctx) {
            try {
                QuestData data = QuestDataAdapter.QUEST_DATA_ADAPTER.fromJson(message.data);
                Quest quest = Quest.getQuest(message.questId);
                if (quest != null) {
                    quest.setQuestData(HardcoreQuesting.proxy.getPlayer(ctx), data);
                }
            } catch (IOException ignored) {
            }
        }
    }
}
