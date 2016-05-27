package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskItems;

import java.io.IOException;

public class QuestDataTaskItems extends QuestDataTask {
    public int[] progress;
    private static final String COUNT = "count";
    private static final String PROGRESS = "progress";

    public QuestDataTaskItems(QuestTask task) {
        super(task);
        this.progress = new int[((QuestTaskItems) task).getItems().length];
    }

    protected QuestDataTaskItems() {
        super();
        this.progress = new int[0];
    }

    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.ITEMS;
    }

    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskItems taskData = new QuestDataTaskItems();
        try {
            taskData.completed = in.nextBoolean();
            int count = in.nextInt();
            taskData.progress = new int[count];
            in.beginArray();
            for (int i = 0; i < count; i++)
                taskData.progress[i] = in.nextInt();
            in.endArray();
        } catch (IOException ignored) {
        }
        return taskData;
    }

    @Override
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(COUNT).value(progress.length);
        out.name(PROGRESS).beginArray();
        for (int i : progress)
            out.value(i);
        out.endArray();
    }

    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.progress = ((QuestDataTaskItems) taskData).progress;
    }
}
