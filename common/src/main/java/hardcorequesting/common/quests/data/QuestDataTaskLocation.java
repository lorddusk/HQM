package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.VisitLocationTask;
import net.minecraft.util.GsonHelper;

public class QuestDataTaskLocation extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String VISITED = "visited";
    public boolean[] visited;
    
    public QuestDataTaskLocation(QuestTask task) {
        super(task);
        this.visited = new boolean[((VisitLocationTask) task).elements.size()];
    }
    
    protected QuestDataTaskLocation() {
        super();
        this.visited = new boolean[0];
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskLocation data = new QuestDataTaskLocation();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.visited = new boolean[GsonHelper.getAsInt(in, COUNT)];
        JsonArray array = GsonHelper.getAsJsonArray(in, VISITED);
        for (int i = 0; i < array.size(); i++) {
            data.visited[i] = array.get(i).getAsBoolean();
        }
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.LOCATION;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, visited.length);
        builder.add(VISITED, Adapter.array(visited).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.visited = ((QuestDataTaskLocation) taskData).visited;
    }
}
