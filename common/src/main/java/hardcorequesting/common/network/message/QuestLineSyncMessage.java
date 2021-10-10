package hardcorequesting.common.network.message;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.io.LocalDataManager;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.SyncUtil;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;

public class QuestLineSyncMessage implements IMessage {
    
    private String reputations, bags, mainDescription;
    private Map<String, String> questsSets;
    
    public QuestLineSyncMessage() {
    }
    
    public QuestLineSyncMessage(QuestLine questLine) {
        this.mainDescription = questLine.mainDescription;
        this.reputations = questLine.reputationManager.saveToString();
        this.bags = questLine.groupTierManager.saveToString();
        this.questsSets = Maps.newLinkedHashMap();
        for (QuestSet set : questLine.questSetsManager.questSets) {
            questsSets.put(set.getFilename(), SaveHandler.save(set, QuestSet.class));
        }
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
    }
    
    public static class Handler implements IMessageHandler<QuestLineSyncMessage, IMessage> {
        
        @Override
        public IMessage onMessage(QuestLineSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(QuestLineSyncMessage message, PacketContext ctx) {
            LocalDataManager dataManager = PlayerDataSyncMessage.cachedDataManager;
            dataManager.provideTemp("description.txt", message.mainDescription);
            dataManager.provideTemp(ReputationManager.FILE_PATH, message.reputations);
            dataManager.provideTemp(GroupTierManager.FILE_PATH, message.bags);
            JsonObject object = new JsonObject();
            JsonArray sets = new JsonArray();
            for (String s : message.questsSets.keySet()) sets.add(s);
            object.add("sets", sets);
            dataManager.provideTemp("sets.json", object.toString());
            for (Map.Entry<String, String> entry : message.questsSets.entrySet()) {
                dataManager.provideTemp("sets/" + entry.getKey() + ".json", entry.getValue());
            }
            QuestLine.getActiveQuestLine().loadAll();
        }
    }
}
