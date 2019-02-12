package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.io.adapter.QuestDataAdapter;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public void fromBytes(ByteBuf buf) {
        this.players = buf.readInt();
        this.questId = new PacketBuffer(buf).readUniqueId();
        int charLength = buf.readInt();
        this.data = buf.readCharSequence(charLength, StandardCharsets.UTF_8).toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.players);
        new PacketBuffer(buf).writeUniqueId(this.questId);
        buf.writeInt(this.data.length());
        buf.writeCharSequence(this.data, StandardCharsets.UTF_8);
        //ByteBufUtils.writeUTF8String(buf, data);
    }

    public static class Handler implements IMessageHandler<QuestDataUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(QuestDataUpdateMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(QuestDataUpdateMessage message, MessageContext ctx) {
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
