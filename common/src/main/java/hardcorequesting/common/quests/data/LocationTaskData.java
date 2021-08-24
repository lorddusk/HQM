package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class LocationTaskData extends TaskData {
    
    private static final String COUNT = "count";
    private static final String VISITED = "visited";
    private final List<Boolean> visited;
    
    public LocationTaskData(int size) {
        super();
        this.visited = new ArrayList<>(size);
        while (visited.size() < size) {
            visited.add(false);
        }
    }
    
    public static TaskData construct(JsonObject in) {
        LocationTaskData data = new LocationTaskData(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, VISITED);
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getAsBoolean())
                data.complete(i);
        }
        return data;
    }
    
    public boolean getValue(int id) {
        if (id >= visited.size())
            return false;
        else return visited.get(id);
    }
    
    public void complete(int id) {
        while (id >= visited.size()) {
            visited.add(false);
        }
        visited.set(id, true);
    }
    
    public void mergeResult(LocationTaskData other) {
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
    public void update(TaskData taskData) {
        super.update(taskData);
        this.visited.clear();
        this.visited.addAll(((LocationTaskData) taskData).visited);
    }
}
