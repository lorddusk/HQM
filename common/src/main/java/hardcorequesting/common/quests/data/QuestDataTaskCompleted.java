package hardcorequesting.common.quests.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskCompleted;

import java.io.IOException;

public class QuestDataTaskCompleted extends QuestDataTask {
    private static final String COUNT = "count";
    private static final String QUESTS = "quests";
    public boolean[] quests;
    
    public QuestDataTaskCompleted(QuestTask task) {
        super(task);
        this.quests = new boolean[((QuestTaskCompleted) task).quests.length];
    }
    
    protected QuestDataTaskCompleted() {
        super();
        this.quests = new boolean[0];
    }
    
    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskCompleted taskData = new QuestDataTaskCompleted();
        try {
            int count = 0;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case QuestDataTask.COMPLETED:
                        taskData.completed = in.nextBoolean();
                        break;
                    case COUNT:
                        count = in.nextInt();
                        taskData.quests = new boolean[count];
                        break;
                    case QUESTS:
                        in.beginArray();
                        for (int i = 0; i < count; i++)
                            taskData.quests[i] = in.nextBoolean();
                        in.endArray();
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
        return QuestTaskAdapter.QuestDataType.COMPLETED;
    }
    
    @Override
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(COUNT).value(quests.length);
        out.name(QUESTS).beginArray();
        for (boolean i : quests)
            out.value(i);
        out.endArray();
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.quests = ((QuestDataTaskCompleted) taskData).quests;
    }
}

