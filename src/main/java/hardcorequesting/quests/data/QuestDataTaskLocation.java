package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskLocation;

import java.io.IOException;

public class QuestDataTaskLocation extends QuestDataTask {
    public boolean[] visited;
    private static final String COUNT = "count";
    private static final String VISITED = "visited";

    public QuestDataTaskLocation(QuestTask task) {
        super(task);
        this.visited = new boolean[((QuestTaskLocation) task).locations.length];
    }

    protected QuestDataTaskLocation() {
        super();
        this.visited = new boolean[0];
    }

    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.LOCATION;
    }

    public static QuestDataTask construct(JsonReader in) {
        QuestDataTaskLocation taskData = new QuestDataTaskLocation();
        try {
            int count = 0;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case QuestDataTask.COMPLETED:
                        taskData.completed = in.nextBoolean();
                        break;
                    case COUNT:
                        count = in.nextInt();
                        taskData.visited = new boolean[count];
                        break;
                    case VISITED:
                        in.beginArray();
                        for (int i = 0; i < count; i++)
                            taskData.visited[i] = in.nextBoolean();
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
    public void write(JsonWriter out) throws IOException {
        super.write(out);
        out.name(COUNT).value(visited.length);
        out.name(VISITED).beginArray();
        for (boolean i : visited)
            out.value(i);
        out.endArray();
    }

    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.visited = ((QuestDataTaskLocation) taskData).visited;
    }
}
