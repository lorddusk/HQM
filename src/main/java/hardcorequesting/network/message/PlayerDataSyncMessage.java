package hardcorequesting.network.message;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.Team;
import hardcorequesting.util.SyncUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PlayerDataSyncMessage implements IMessage {

    private boolean local, serverWorld, questing, hardcore;
    private String team;

    private List<String> data = new ArrayList<>();

    public PlayerDataSyncMessage() {
    }

    public PlayerDataSyncMessage(boolean local, boolean serverWorld, EntityPlayer player) {
        this.local = local;
        this.serverWorld = serverWorld;
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
        this.team = Team.saveTeam(player);
        String stringData = QuestingData.saveQuestingData(player);
        this.data = SyncUtil.splitData(stringData, 3000);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.local = buf.readBoolean();
        this.serverWorld = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        this.team = ByteBufUtils.readUTF8String(buf);
        int count = buf.readInt();
        data.clear();
        for (int i = 0; i<count; i++) {
            data.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.serverWorld);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        ByteBufUtils.writeUTF8String(buf, team);
        buf.writeInt(this.data.size());
        for (String val : this.data) {
            ByteBufUtils.writeUTF8String(buf, val);
        }
    }

    public static class Handler implements IMessageHandler<PlayerDataSyncMessage, IMessage> {
        
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(PlayerDataSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }
    
        @SideOnly(Side.CLIENT)
        private void handle(PlayerDataSyncMessage message, MessageContext ctx) {
            /* Why copying our files if we get all quests from the server anyway? It could lead to wrong questlines
            if (!QuestLine.doServerSync) // Copy defaults when server sync is off
                SaveHandler.copyFolder(SaveHandler.getDefaultFolder(), SaveHandler.getRemoteFolder());
                */
            try {
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("teams"))) {
                    out.print("[");
                    out.print(message.team);
                    out.print("]");
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("data"))) {
                    out.print("[");
                    out.print(SyncUtil.joinData(message.data));
                    out.print("]");
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("state"))) {
                    out.print(SaveHandler.saveQuestingState(message.questing, message.hardcore));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            QuestLine.receiveServerSync(Minecraft.getMinecraft().player, message.local, message.serverWorld);
        }
    }
}
