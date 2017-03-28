package hardcorequesting.network.message;

import hardcorequesting.quests.QuestingData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LivesUpdate implements IMessage {

    private String uuid;
    private int lives;

    public LivesUpdate() {
    }

    public LivesUpdate(String uuid, int lives) {
        this.uuid = uuid;
        this.lives = lives;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.lives = buf.readInt();
        int size = buf.readInt();
        this.uuid = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.lives);
        buf.writeInt(this.uuid.getBytes().length);
        buf.writeBytes(this.uuid.getBytes());
    }

    public static class Handler implements IMessageHandler<LivesUpdate, IMessage> {

        @Override
        public IMessage onMessage(LivesUpdate message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(LivesUpdate message, MessageContext ctx) {
            QuestingData.getQuestingData(message.uuid).setRawLives(message.lives);
        }
    }
}
