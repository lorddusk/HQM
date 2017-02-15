package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.io.adapter.QuestDataAdapter;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class QuestDataUpdateMessage implements IMessage {

    private String id, data;
    private int players;

    public QuestDataUpdateMessage() {
    }

    public QuestDataUpdateMessage(String id, int players, QuestData data) {
        try {
            this.id = id;
            this.data = QuestDataAdapter.QUEST_DATA_ADAPTER.toJson(data);
            this.players = players;
        } catch (IOException ignored) {
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.players = buf.readInt();
        int size = buf.readInt();
        this.id = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.players);
        buf.writeInt(this.id.getBytes().length);
        buf.writeBytes(this.id.getBytes());
        buf.writeInt(this.data.getBytes().length);
        buf.writeBytes(this.data.getBytes());
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
                    quest.preRead(message.players, data);
                    quest.setQuestData(HardcoreQuesting.proxy.getPlayer(ctx), data);
                }
            } catch (IOException ignored) {
            }
        }
    }
}
