package hardcorequesting.common.network.message;

import com.google.gson.*;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.adapter.QuestDataAdapter;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestData;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class QuestDataUpdateMessage implements IMessage {
    
    private static final Gson GSON = new Gson();
    private UUID questId;
    private String data;
    private int players;
    
    public QuestDataUpdateMessage() {
    }
    
    public QuestDataUpdateMessage(UUID questId, int players, QuestData data) {
        this.questId = questId;
        this.data = GSON.toJson(QuestDataAdapter.serialize(data));
        this.players = players;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.players = buf.readInt();
        this.questId = buf.readUUID();
        int charLength = buf.readInt();
        this.data = buf.readCharSequence(charLength, StandardCharsets.UTF_8).toString();
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.players);
        buf.writeUUID(this.questId);
        buf.writeInt(this.data.length());
        buf.writeCharSequence(this.data, StandardCharsets.UTF_8);
    }
    
    public static class Handler implements IMessageHandler<QuestDataUpdateMessage, IMessage> {
        
        @Override
        public IMessage onMessage(QuestDataUpdateMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(QuestDataUpdateMessage message, PacketContext ctx) {
            try {
                Quest quest = Quest.getQuest(message.questId);
                if (quest != null) {
                    QuestData data = QuestDataAdapter.deserialize(GSON.fromJson(message.data, JsonObject.class), quest);
                    quest.setQuestData(HardcoreQuestingCore.proxy.getPlayer(ctx), data);
                }
            } catch (JsonSyntaxException ignored) {
            }
        }
    }
}
