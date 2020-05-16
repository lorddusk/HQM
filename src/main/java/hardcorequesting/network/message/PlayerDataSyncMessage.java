package hardcorequesting.network.message;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.Team;
import hardcorequesting.util.SyncUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.io.PrintWriter;

public class PlayerDataSyncMessage implements IMessage {
    
    private boolean local, serverWorld, questing, hardcore;
    private String team;
    private String data;
    
    public PlayerDataSyncMessage() {
    }
    
    public PlayerDataSyncMessage(boolean local, boolean serverWorld, PlayerEntity player) {
        this.local = local;
        this.serverWorld = serverWorld;
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
        this.team = Team.saveTeam(player);
        this.data = QuestingData.saveQuestingData(player);
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.local = buf.readBoolean();
        this.serverWorld = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        this.team = buf.readString(32767);
        this.data = SyncUtil.readLargeString(buf);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.serverWorld);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        buf.writeString(team);
        SyncUtil.writeLargeString(this.data, buf);
    }
    
    public static class Handler implements IMessageHandler<PlayerDataSyncMessage, IMessage> {
        
        @Environment(EnvType.CLIENT)
        @Override
        public IMessage onMessage(PlayerDataSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        @Environment(EnvType.CLIENT)
        private void handle(PlayerDataSyncMessage message, PacketContext ctx) {
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
            QuestLine.receiveServerSync(MinecraftClient.getInstance().player, message.local, message.serverWorld);
        }
    }
}
