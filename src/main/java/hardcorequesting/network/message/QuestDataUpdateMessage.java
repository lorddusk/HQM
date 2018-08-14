package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.io.adapter.QuestDataAdapter;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.Charsets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class QuestDataUpdateMessage implements IMessage {

    private String id, data;
    private int players;

    public QuestDataUpdateMessage() {
    }

    public QuestDataUpdateMessage(String id, int players, QuestData data) {
        this.id = id;
        this.data = QuestDataAdapter.QUEST_DATA_ADAPTER.toJson(data);
        this.players = players;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.players = buf.readInt();
        this.id = ByteBufUtils.readUTF8String(buf);
        int charLength = buf.readInt();
        this.data = buf.readCharSequence(charLength, StandardCharsets.UTF_8).toString();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.players);
        ByteBufUtils.writeUTF8String(buf, id);
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
                Quest quest = Quest.getQuest(message.id);
                if (quest != null) {
                    quest.setQuestData(HardcoreQuesting.proxy.getPlayer(ctx), data);
                }
            } catch (IOException ignored) {
            }
        }
    }
}
