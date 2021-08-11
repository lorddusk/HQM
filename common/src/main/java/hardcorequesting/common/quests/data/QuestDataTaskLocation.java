package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class QuestDataTaskLocation extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String VISITED = "visited";
    private final List<Boolean> visited;
    
    public QuestDataTaskLocation(QuestTask task) {
        this(((VisitLocationTask) task).elements.size());
    }
    
    protected QuestDataTaskLocation(int size) {
        super();
        this.visited = new ArrayList<>(size);
        while (visited.size() < size) {
            visited.add(false);
        }
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskLocation data = new QuestDataTaskLocation(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, VISITED);
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getAsBoolean())
                data.complete(i);
        }
        return data;
    }
    
    public boolean getValue(int id) {
        if (visited.size() <= id)
            return false;
        else return visited.get(id);
    }
    
    public void complete(int id) {
        while (id >= visited.size()) {
            visited.add(false);
        }
        visited.set(id, true);
    }
    
    public void clear() {
        visited.clear();
    }
    
    public void mergeResult(QuestDataTaskLocation other) {
        for (int i = 0; i < other.visited.size(); i++) {
            if (other.visited.get(i))
                complete(i);
        }
    }
    
    public float getCompletedRatio(int size) {
        return (float) visited.stream().limit(size).filter(Boolean::booleanValue).count() / size;
    }
    
    public boolean areAllCompleted(int size) {
        return visited.stream().limit(size).allMatch(Boolean::booleanValue);
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.LOCATION;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, visited.size());
        builder.add(VISITED, Adapter.array(visited.toArray()).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.visited.clear();
        this.visited.addAll(((QuestDataTaskLocation) taskData).visited);
    }
}
