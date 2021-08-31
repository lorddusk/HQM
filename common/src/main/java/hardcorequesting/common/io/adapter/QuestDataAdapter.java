package hardcorequesting.common.io.adapter;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.data.TaskData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QuestDataAdapter {
    
    public static final TypeAdapter<QuestData> QUEST_DATA_ADAPTER = new TypeAdapter<>() {
        public static final String PLAYERS = "players";
        public static final String REWARDS = "rewards";
        public static final String COMPLETED = "completed";
        public static final String CLAIMED = "claimed";
        public static final String TASKS = "tasks";
        public static final String TASKS_SIZE = "tasksSize";
        public static final String AVAILABLE = "available";
        public static final String TIME = "time";
        
        @Override
        public void write(JsonWriter out, QuestData value) throws IOException {
            out.beginObject();
            out.name(REWARDS).beginArray();
            for (boolean canClaim : value.getRewardsForSerialization())
                out.value(canClaim);
            out.endArray();
            out.name(COMPLETED).value(value.completed);
            out.name(CLAIMED).value(value.teamRewardClaimed);
            out.name(AVAILABLE).value(value.available);
            out.name(TIME).value(value.time);
            out.name(TASKS).beginArray();
            for (TaskData data : value.getTaskDataForSerialization()) {
                if (data != null)
                    QuestTaskAdapter.QUEST_DATA_TASK_ADAPTER.write(out, data);
                else Streams.write(new JsonObject(), out);  //Add empty object to keep order of task data
            }
            out.endArray();
            out.endObject();
        }
        
        @Override
        public QuestData read(JsonReader in) throws IOException {
            QuestData data = new QuestData();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case PLAYERS:
                    case TASKS_SIZE:
                        in.nextInt();   //Sizes are no longer used. Read it for backwards-compatibility
                        break;
                    case REWARDS:
                        in.beginArray();
                        List<Boolean> claimableRewards = new ArrayList<>();
                        while (in.hasNext())
                            claimableRewards.add(in.nextBoolean());
                        data.setRewardsFromSerialization(claimableRewards);
                        in.endArray();
                        break;
                    case COMPLETED:
                        data.completed = in.nextBoolean();
                        break;
                    case CLAIMED:
                        data.teamRewardClaimed = in.nextBoolean();
                        break;
                    case AVAILABLE:
                        data.available = in.nextBoolean();
                        break;
                    case TIME:
                        data.time = in.nextLong();
                        break;
                    case TASKS:
                        in.beginArray();
                        List<TaskData> taskData = new ArrayList<>();
                        while (in.hasNext())
                            taskData.add(QuestTaskAdapter.QUEST_DATA_TASK_ADAPTER.read(in));
                        data.setTaskDataFromSerialization(taskData);
                        in.endArray();
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            return data;
        }
    };
    
}
