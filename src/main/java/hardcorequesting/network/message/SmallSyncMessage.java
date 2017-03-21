package hardcorequesting.network.message;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.io.PrintWriter;

public class SmallSyncMessage implements IMessage {

    private boolean questing, hardcore;
    private String teams, data;

    public SmallSyncMessage() {
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
        this.teams = SaveHandler.saveTeams();
        this.data = SaveHandler.saveQuestingData();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        int size = buf.readInt();
        this.teams = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        buf.writeInt(this.teams.getBytes().length);
        buf.writeBytes(this.teams.getBytes());
        buf.writeInt(this.data.getBytes().length);
        buf.writeBytes(this.data.getBytes());
    }

    public static class Handler implements IMessageHandler<SmallSyncMessage, IMessage> {

        @Override
        public IMessage onMessage(SmallSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(SmallSyncMessage message, MessageContext ctx) {
            SaveHandler.copyFolder(SaveHandler.getDefaultFolder(), SaveHandler.getRemoteFolder());
            try {
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("teams"))) {
                    out.print(message.teams);
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("data"))) {
                    out.print(message.data);
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("state"))) {
                    out.print(SaveHandler.saveQuestingState(message.questing, message.hardcore));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            QuestLine.receiveServerSync(false, true);
        }
    }
}
