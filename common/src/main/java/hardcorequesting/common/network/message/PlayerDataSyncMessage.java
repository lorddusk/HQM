package hardcorequesting.common.network.message;

import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.io.LocalDataManager;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.TeamManager;
import hardcorequesting.common.util.SyncUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class PlayerDataSyncMessage implements IMessage {
    
    private boolean local, remote, questing, hardcore;
    private String teams;
    private String data;
    private String deaths;
    
    public PlayerDataSyncMessage() {
    }
    
    public PlayerDataSyncMessage(QuestLine questLine, boolean local, boolean remote, Player player) {
        this.local = local;
        this.remote = remote;
        this.questing = questLine.questingDataManager.isQuestActive();
        this.hardcore = questLine.questingDataManager.isHardcoreActive();
        this.teams = questLine.teamManager.saveToString(player);
        this.deaths = questLine.deathStatsManager.saveToString();
        this.data = questLine.questingDataManager.data.saveToString(player);
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.local = buf.readBoolean();
        this.remote = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        this.teams = SyncUtil.readLargeString(buf);
        this.deaths = SyncUtil.readLargeString(buf);
        this.data = SyncUtil.readLargeString(buf);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.remote);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        SyncUtil.writeLargeString(this.teams, buf);
        SyncUtil.writeLargeString(this.deaths, buf);
        SyncUtil.writeLargeString(this.data, buf);
    }
    
    @Deprecated //Temporary while loading is becoming more sound
    public static LocalDataManager cachedDataManager = new LocalDataManager();
    
    public static class Handler implements IMessageHandler<PlayerDataSyncMessage, IMessage> {
        
        @Environment(EnvType.CLIENT)
        @Override
        public IMessage onMessage(PlayerDataSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        @Environment(EnvType.CLIENT)
        private void handle(PlayerDataSyncMessage message, PacketContext ctx) {
            cachedDataManager = new LocalDataManager();
            cachedDataManager.provideTemp(TeamManager.FILE_PATH, message.teams);
            cachedDataManager.provideTemp(QuestingDataManager.DATA_FILE_PATH, message.data);
            cachedDataManager.provideTemp(QuestingDataManager.STATE_FILE_PATH, QuestingDataManager.saveQuestingState(message.questing, message.hardcore));
            cachedDataManager.provideTemp(DeathStatsManager.FILE_PATH, message.deaths);
            QuestLine.receiveDataFromServer(cachedDataManager);
            QuestLine.getActiveQuestLine().loadAll(cachedDataManager);
        }
    }
}
