package hardcorequesting.network.message;

import hardcorequesting.quests.QuestingData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class CloseBookMessage implements IMessage {

    private UUID playerID;

    public CloseBookMessage() {
    }

    public CloseBookMessage(UUID playerID) {
        this.playerID = playerID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerID = new PacketBuffer(buf).readUniqueId();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        new PacketBuffer(buf).writeUniqueId(this.playerID);
    }

    public static class Handler implements IMessageHandler<CloseBookMessage, IMessage> {

        @Override
        public IMessage onMessage(CloseBookMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(CloseBookMessage message, MessageContext ctx) {
            QuestingData.getQuestingData(message.playerID).getTeam().getEntry(message.playerID).setBookOpen(false);
        }
    }
}
