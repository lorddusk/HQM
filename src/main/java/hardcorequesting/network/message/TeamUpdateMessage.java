package hardcorequesting.network.message;

import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.TeamUpdateType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeamUpdateMessage implements IMessage {

    private TeamUpdateType type;
    private String data;

    public TeamUpdateMessage() {
    }

    public TeamUpdateMessage(TeamUpdateType type, String data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = TeamUpdateType.values()[buf.readInt()];
        data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.type.ordinal());
        ByteBufUtils.writeUTF8String(buf, data);

        //buf.writeInt(this.data.getBytes().length);
        
        //for(int i = 0; i < data.getBytes().length; i++)
          //  buf.writeByte(data.getBytes()[i]);
    }

    public static class Handler implements IMessageHandler<TeamUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(TeamUpdateMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(TeamUpdateMessage message, MessageContext ctx) {
            message.type.update(QuestingData.getQuestingData(ctx.getServerHandler().player).getTeam(), message.data);
        }
    }
}
