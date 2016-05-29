package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;

import java.io.IOException;

public class QuestDataTaskReputationKill extends QuestDataTask {
    public int kills;
    private static final String KILLS = "kills";

    public QuestDataTaskReputationKill(QuestTask task) {
        super(task);
    }

    protected QuestDataTaskReputationKill() {
        super();
    }

    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.REPUTATION_KILL;
    }

    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskReputationKill taskData = new QuestDataTaskReputationKill();
        try {
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case QuestDataTask.COMPLETED:
                        taskData.completed = in.nextBoolean();
                        break;
                    case KILLS:
                        taskData.kills = in.nextInt();
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
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(KILLS).value(kills);
    }

    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.kills = ((QuestDataTaskReputationKill) taskData).kills;
    }
}
