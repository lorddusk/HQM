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

public class PlayerDataSyncMessage implements IMessage {

    private boolean local, serverWorld, questing, hardcore;
    private String team;
    private String data;

    public PlayerDataSyncMessage() {
    }

    public PlayerDataSyncMessage(boolean local, boolean serverWorld, EntityPlayer player) {
        this.local = local;
        this.serverWorld = serverWorld;
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
        this.team = Team.saveTeam(player);
        this.data = QuestingData.saveQuestingData(player);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.local = buf.readBoolean();
        this.serverWorld = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        this.team = ByteBufUtils.readUTF8String(buf);
        this.data = SyncUtil.readLargeString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.serverWorld);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        ByteBufUtils.writeUTF8String(buf, team);
        SyncUtil.writeLargeString(this.data, buf);
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
                    out.print(message.data);
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
