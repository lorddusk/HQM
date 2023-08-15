package hardcorequesting.common.network.message;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.LocalDataManager;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.SyncUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class PlayerDataSyncMessage implements IMessage {
    
    private String reputations, bags, mainDescription;
    private Map<String, String> questsSets;
    
    private boolean questing;
    private boolean hardcore;
    private String teams;
    private String data;
    private String deaths;
    
    public PlayerDataSyncMessage() {
    }
    
    public PlayerDataSyncMessage(QuestLine questLine, Player player) {
        this.mainDescription = questLine.getMainDescription();
        this.reputations = questLine.reputationManager.saveToString();
        this.bags = questLine.groupTierManager.saveToString();
        this.questsSets = Maps.newLinkedHashMap();
        for (QuestSet set : questLine.questSetsManager.questSets) {
            questsSets.put(set.getFilename(), SaveHandler.save(set, QuestSet.class));
        }
        this.questing = questLine.questingDataManager.isQuestActive();
        this.hardcore = questLine.questingDataManager.isHardcoreActive();
        this.teams = questLine.teamManager.saveToString(player);
        this.deaths = questLine.deathStatsManager.saveToString();
        this.data = questLine.questingDataManager.data.saveToString(player);
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        mainDescription = SyncUtil.readLargeString(buf);
        reputations = SyncUtil.readLargeString(buf);
        bags = SyncUtil.readLargeString(buf);
        
        int size = buf.readInt();
        this.questsSets = Maps.newLinkedHashMap();
        for (int i = 0; i < size; i++) {
            String fileName = SyncUtil.readLargeString(buf);
            String questSetString = SyncUtil.readLargeString(buf);
            this.questsSets.put(fileName, questSetString);
        }
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        this.teams = SyncUtil.readLargeString(buf);
        this.deaths = SyncUtil.readLargeString(buf);
        this.data = SyncUtil.readLargeString(buf);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        SyncUtil.writeLargeString(mainDescription, buf);
        SyncUtil.writeLargeString(reputations, buf);
        SyncUtil.writeLargeString(bags, buf);
        
        buf.writeInt(this.questsSets.size());
        for (Map.Entry<String, String> entry : this.questsSets.entrySet()) {
            SyncUtil.writeLargeString(entry.getKey(), buf);
            SyncUtil.writeLargeString(entry.getValue(), buf);
        }
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        SyncUtil.writeLargeString(this.teams, buf);
        SyncUtil.writeLargeString(this.deaths, buf);
        SyncUtil.writeLargeString(this.data, buf);
    }
    
    public static class Handler implements IMessageHandler<PlayerDataSyncMessage, IMessage> {
        
        @Environment(EnvType.CLIENT)
        @Override
        public IMessage onMessage(PlayerDataSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message));
            return null;
        }
        
        @Environment(EnvType.CLIENT)
        private void handle(PlayerDataSyncMessage message) {
            QuestLine questLine = QuestLine.reset();
            
            JsonArray sets = new JsonArray();
            for (String s : message.questsSets.keySet()) sets.add(s);
            JsonObject object = new JsonObject();
            object.add("sets", sets);
            
            LocalDataManager data = new LocalDataManager();
            data.provide("sets.json", object.toString());
            for (Map.Entry<String, String> entry : message.questsSets.entrySet()) {
                data.provide("sets/" + entry.getKey() + ".json", entry.getValue());
            }
            questLine.questSetsManager.load(data);
            
            questLine.setMainDescription(message.mainDescription);
            questLine.reputationManager.clearAndLoad(message.reputations);
            questLine.groupTierManager.clearAndLoad(message.bags);
            
            questLine.teamManager.loadFromString(message.teams);
            questLine.questingDataManager.data.loadFromString(message.data);
            questLine.questingDataManager.state.loadFromString(QuestingDataManager.saveQuestingState(message.questing, message.hardcore));
            questLine.deathStatsManager.loadFromString(message.deaths);
        }
    }
}
