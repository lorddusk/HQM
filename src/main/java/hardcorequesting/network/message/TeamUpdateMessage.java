package hardcorequesting.network.message;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.TeamUpdateType;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class TeamUpdateMessage implements IMessage {

    private TeamUpdateType type;
    private List<String> data = new ArrayList<String>();

    public TeamUpdateMessage() {
    }

    private List<String> SplitData (String input, int size) {
        List<String> output = new ArrayList<String>();
        int len = input.length();
        for (int i=0; i<len; i+= size) {
            output.add(input.substring(i, Math.min(len, i+size)));
        }

        return output;
    }

    public TeamUpdateMessage(TeamUpdateType type, String data) {
        this.type = type;
        this.data = SplitData(data, 3000);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = TeamUpdateType.values()[buf.readInt()];
        int count = buf.readInt();
        this.data.clear();
        for (int i = 0; i<count; i++) {
            this.data.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.type.ordinal());

        buf.writeInt(this.data.size());
        for (String chunk : this.data) {
            ByteBufUtils.writeUTF8String(buf, chunk);
        }

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
            message.type.update(QuestingData.getQuestingData(HardcoreQuesting.proxy.getPlayer(ctx)).getTeam(), String.join("", message.data));
        }
    }
}
