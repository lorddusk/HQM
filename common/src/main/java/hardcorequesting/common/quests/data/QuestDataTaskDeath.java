package hardcorequesting.common.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;

import java.io.IOException;

public class QuestDataTaskDeath extends QuestDataTask {
    
    private static final String DEATHS = "deaths";
    public int deaths;
    
    public QuestDataTaskDeath(QuestTask task) {
        super(task);
    }
    
    protected QuestDataTaskDeath() {
        super();
    }
    
    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskDeath taskData = new QuestDataTaskDeath();
        try {
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case QuestDataTask.COMPLETED:
                        taskData.completed = in.nextBoolean();
                        break;
                    case DEATHS:
                        taskData.deaths = in.nextInt();
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException ignored) {
        }
        return taskData;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.DEATH;
    }
    
    @Override
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(DEATHS).value(deaths);
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.deaths = ((QuestDataTaskDeath) taskData).deaths;
    }
}
