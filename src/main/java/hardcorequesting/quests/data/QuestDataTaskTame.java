package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskTame;

import java.io.IOException;

public class QuestDataTaskTame extends QuestDataTask {

    private static final String COUNT = "count";
    private static final String TAMED = "tamed";
    public int[] tamed;

    public QuestDataTaskTame(QuestTask task) {
        super(task);
        this.tamed = new int[((QuestTaskTame) task).tames.length];
    }

    protected QuestDataTaskTame() {
        super();
        this.tamed = new int[0];
    }

    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskTame taskData = new QuestDataTaskTame();
        try {
            int count = 0;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case QuestDataTask.COMPLETED:
                        taskData.completed = in.nextBoolean();
                        break;
                    case COUNT:
                        count = in.nextInt();
                        taskData.tamed = new int[count];
                        break;
                    case TAMED:
                        in.beginArray();
                        for (int i = 0; i < count; i++)
                            taskData.tamed[i] = in.nextInt();
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
        return QuestTaskAdapter.QuestDataType.TAME;
    }

    @Override
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(COUNT).value(tamed.length);
        out.name(TAMED).beginArray();
        for (int i : tamed)
            out.value(i);
        out.endArray();
    }

    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.tamed = ((QuestDataTaskTame) taskData).tamed;
    }
}
