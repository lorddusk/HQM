package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskMob;

import java.io.IOException;

public class QuestDataTaskMob extends QuestDataTask {
    public int[] killed;
    private static final String COUNT = "count";
    private static final String KILLED = "killed";

    public QuestDataTaskMob(QuestTask task) {
        super(task);
        this.killed = new int[((QuestTaskMob) task).mobs.length];
    }

    protected QuestDataTaskMob() {
        super();
        this.killed = new int[0];
    }

    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.MOB;
    }

    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskMob taskData = new QuestDataTaskMob();
        try {
            taskData.completed = in.nextBoolean();
            int count = in.nextInt();
            taskData.killed = new int[count];
            in.beginArray();
            for (int i = 0; i < count; i++)
                taskData.killed[i] = in.nextInt();
            in.endArray();
        } catch (IOException ignored) {
        }
        return taskData;
    }

    @Override
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(COUNT).value(killed.length);
        out.name(KILLED).beginArray();
        for (int i : killed)
            out.value(i);
        out.endArray();
    }

    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.killed = ((QuestDataTaskMob) taskData).killed;
    }
}
