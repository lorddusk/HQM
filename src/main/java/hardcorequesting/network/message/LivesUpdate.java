package hardcorequesting.network.message;

import hardcorequesting.quests.QuestingData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public void fromBytes(ByteBuf buf) {
        this.lives = buf.readInt();
        uuid = new PacketBuffer(buf).readUniqueId();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.lives);
        new PacketBuffer(buf).writeUniqueId(this.uuid);
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
