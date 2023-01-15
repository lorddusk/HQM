package hardcorequesting.common.io.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.quests.task.QuestTask;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestDataAdapter {
    public static final String REWARDS = "rewards";
    public static final String COMPLETED = "completed";
    public static final String CLAIMED = "claimed";
    public static final String TASKS = "tasks";
    public static final String AVAILABLE = "available";
    public static final String TIME = "time";
    
    public static JsonElement serialize(QuestData data) {
        JsonObject json = new JsonObject();
        JsonArray rewards = new JsonArray();
        for (boolean canClaim : data.getRewardsForSerialization())
            rewards.add(canClaim);
        json.add(REWARDS, rewards);
        json.addProperty(COMPLETED, data.completed);
        json.addProperty(CLAIMED, data.teamRewardClaimed);
        json.addProperty(AVAILABLE, data.available);
        json.addProperty(TIME, data.time);
        JsonArray tasks = new JsonArray();
        for (TaskData taskData : data.getTaskDataForSerialization()) {
            if (taskData != null) {
                Adapter.JsonObjectBuilder builder = Adapter.object();
                taskData.write(builder);
                tasks.add(builder.build());
            }
            else
                tasks.add(new JsonObject());  //Add empty object to keep order of task data
        }
        json.add(TASKS, tasks);
        
        return json;
    }
    
    public static QuestData deserialize(JsonObject json, Quest quest) {
        Objects.requireNonNull(quest);
        QuestData data = new QuestData();
        
        JsonArray rewardsJson = GsonHelper.getAsJsonArray(json, REWARDS);
        List<Boolean> claimableRewards = new ArrayList<>();
        for (JsonElement canClaim : rewardsJson)
            claimableRewards.add(GsonHelper.convertToBoolean(canClaim, "reward"));
        data.setRewardsFromSerialization(claimableRewards);
        
        data.completed = GsonHelper.getAsBoolean(json, COMPLETED);
        data.teamRewardClaimed = GsonHelper.getAsBoolean(json, CLAIMED);
        data.available = GsonHelper.getAsBoolean(json, AVAILABLE);
        data.time = GsonHelper.getAsLong(json, TIME);
        
        JsonArray tasksJson = GsonHelper.getAsJsonArray(json, TASKS);
        List<TaskData> taskData = new ArrayList<>();
        for (int i = 0; i < tasksJson.size(); i++) {
            JsonElement taskJson = tasksJson.get(i);
            QuestTask<?> task = quest.getTasks().get(i);
            
            taskData.add(task.loadData(taskJson.getAsJsonObject()));
        }
        data.setTaskDataFromSerialization(taskData);
        
        return data;
    }
}
