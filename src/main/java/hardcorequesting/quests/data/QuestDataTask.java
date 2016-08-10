package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;

import java.io.IOException;

public class QuestDataTask {
    public boolean completed;
    protected static final String COMPLETED = "completed";

    public QuestDataTask(QuestTask task) {
    }

    protected QuestDataTask() {

    }

    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.GENERIC;
    }

    public static QuestDataTask construct(JsonReader in) {
        QuestDataTask data = new QuestDataTask();
        try {
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case COMPLETED:
                        data.completed = in.nextBoolean();
                        break;
                }
            }
        } catch (IOException ignored) {
        }
        return data;
    }

    public void write(JsonWriter out) throws IOException {
        out.name(COMPLETED).value(completed);
    }

    public void update(QuestDataTask taskData) {
        this.completed = taskData.completed;
    }
}
