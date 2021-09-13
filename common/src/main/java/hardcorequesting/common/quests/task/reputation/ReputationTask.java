package hardcorequesting.common.quests.task.reputation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.client.ReputationTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ReputationTask<Data extends TaskData> extends QuestTask<Data> {
    //for this task to be completed, all reputation settings (up to 4) has to be completed at the same time, therefore it's not saved whether you've completed one of these reputation settings, just if you've completed it all
    private static final String REPUTATION = "reputation";
    
    protected static final int LIMIT = 4;
    
    protected final PartList<Part> parts = new PartList<>(Part::new, EditType.Type.REPUTATION_TASK, LIMIT);
    
    public ReputationTask(Class<Data> dataType, Quest parent, String description, String longDescription) {
        super(dataType, parent, description, longDescription);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public TaskGraphic createGraphic(UUID playerId) {
        return new ReputationTaskGraphic(this, parts, playerId);
    }
    
    @Deprecated
    public List<Part> getSettings() {
        return parts.getElements();
    }
    
    public void setSetting(int id, Part setting) {
        parts.set(id, setting);
    }
    
    protected boolean isPlayerInRange(Player player) {
        if (!parts.isEmpty()) {
            
            TaskData data = getData(player);
            if (!data.completed && !player.getCommandSenderWorld().isClientSide) {
                for (Part setting : parts) {
                    if (!setting.isValid(QuestingDataManager.getInstance().getQuestingData(player).getTeam())) {
                        return false;
                    }
                }
                
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void mergeProgress(UUID playerID, Data own, Data other) {
        own.completed |= other.completed;
    }
    
    @Override
    public void setComplete(Data data) {
        data.completed = true;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(REPUTATION, parts.write(QuestTaskAdapter.REPUTATION_TASK_ADAPTER));
    }
    
    @Override
    public void read(JsonObject object) {
        List<QuestTaskAdapter.ReputationSettingConstructor> list = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, REPUTATION, new JsonArray())) {
            QuestTaskAdapter.ReputationSettingConstructor constructor = QuestTaskAdapter.ReputationSettingConstructor.read(element);
            if (constructor != null)
                list.add(constructor);
        }
        QuestTaskAdapter.taskReputationListMap.put(this, list);
    }
    
    public static class Part {
        
        private final Reputation reputation;
        private ReputationMarker lower;
        private ReputationMarker upper;
        private final boolean inverted;
        
        private Part() {
            this(null, null, null, false);
        }
        
        public Part(Reputation reputation, ReputationMarker lower, ReputationMarker upper, boolean inverted) {
            this.reputation = reputation;
            this.lower = lower;
            this.upper = upper;
            this.inverted = inverted;
        }
        
        public Reputation getReputation() {
            return reputation;
        }
        
        public ReputationMarker getLower() {
            return lower;
        }
        
        public void setLower(ReputationMarker lower) {
            this.lower = lower;
        }
        
        public ReputationMarker getUpper() {
            return upper;
        }
        
        public void setUpper(ReputationMarker upper) {
            this.upper = upper;
        }
        
        public boolean isInverted() {
            return inverted;
        }
        
        public boolean isValid(Team team) {
            if (getReputation() == null || !getReputation().isValid()) {
                return false;
            }
            ReputationMarker current = getReputation().getCurrentMarker(team.getReputation(this.getReputation()));
            
            return ((lower == null || lower.getValue() <= current.getValue()) && (upper == null || current.getValue() <= upper.getValue())) != inverted;
        }
    }
}
