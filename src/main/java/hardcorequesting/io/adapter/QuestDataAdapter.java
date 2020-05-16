package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.data.QuestDataTask;

import java.io.IOException;

public class QuestDataAdapter {
    
    public static final TypeAdapter<QuestData> QUEST_DATA_ADAPTER = new TypeAdapter<QuestData>() {
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
            out.name(PLAYERS).value(value.reward.length);
            out.name(REWARDS).beginArray();
            for (boolean bool : value.reward)
                out.value(bool);
            out.endArray();
            out.name(COMPLETED).value(value.completed);
            out.name(CLAIMED).value(value.claimed);
            out.name(AVAILABLE).value(value.available);
            out.name(TIME).value(value.time);
            out.name(TASKS_SIZE).value(value.tasks.length);
            out.name(TASKS).beginArray();
            for (QuestDataTask task : value.tasks)
                if (task != null)
                    QuestTaskAdapter.QUEST_DATA_TASK_ADAPTER.write(out, task);
            out.endArray();
            out.endObject();
        }
        
        @Override
        public QuestData read(JsonReader in) throws IOException {
            QuestData data = new QuestData(1);
            in.beginObject();
            int i;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case PLAYERS:
                        data = new QuestData(in.nextInt());
                        break;
                    case REWARDS:
                        in.beginArray();
                        i = 0;
                        while (in.hasNext() && i < data.reward.length)
                            data.reward[i++] = in.nextBoolean();
                        in.endArray();
                        break;
                    case COMPLETED:
                        data.completed = in.nextBoolean();
                        break;
                    case CLAIMED:
                        data.claimed = in.nextBoolean();
                        break;
                    case AVAILABLE:
                        data.available = in.nextBoolean();
                        break;
                    case TIME:
                        data.time = in.nextInt();
                        break;
                    case TASKS_SIZE:
                        data.tasks = new QuestDataTask[in.nextInt()];
                        break;
                    case TASKS:
                        in.beginArray();
                        i = 0;
                        while (in.hasNext() && i < data.tasks.length)
                            data.tasks[i++] = QuestTaskAdapter.QUEST_DATA_TASK_ADAPTER.read(in);
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
