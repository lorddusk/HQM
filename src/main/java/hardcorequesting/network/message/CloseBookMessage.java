package hardcorequesting.network.message;

import hardcorequesting.quests.QuestingData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CloseBookMessage implements IMessage {

    private String uuid;

    public CloseBookMessage() {
    }

    public CloseBookMessage(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        this.uuid = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.uuid.getBytes().length);
        buf.writeBytes(this.uuid.getBytes());
    }

    public static class Handler implements IMessageHandler<CloseBookMessage, IMessage> {

        @Override
        public IMessage onMessage(CloseBookMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(CloseBookMessage message, MessageContext ctx) {
            QuestingData.getQuestingData(message.uuid).getTeam().getEntry(message.uuid).setBookOpen(false);
        }
    }
}
