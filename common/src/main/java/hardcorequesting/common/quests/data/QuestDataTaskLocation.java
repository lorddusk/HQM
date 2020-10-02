package hardcorequesting.common.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskLocation;

import java.io.IOException;

public class QuestDataTaskLocation extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String VISITED = "visited";
    public boolean[] visited;
    
    public QuestDataTaskLocation(QuestTask task) {
        super(task);
        this.visited = new boolean[((QuestTaskLocation) task).locations.length];
    }
    
    protected QuestDataTaskLocation() {
        super();
        this.visited = new boolean[0];
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
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.LOCATION;
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
