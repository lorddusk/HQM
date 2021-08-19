package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class QuestDataTaskMob extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String KILLED = "killed";
    private final List<Integer> killed;
    
    public QuestDataTaskMob(int size) {
        super();
        this.killed = new ArrayList<>(size);
        while (killed.size() < size) {
            killed.add(0);
        }
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskMob data = new QuestDataTaskMob(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, KILLED);
        for (int i = 0; i < array.size(); i++) {
            data.setValue(i, array.get(i).getAsInt());
        }
        return data;
    }
    
    public int getValue(int id) {
        if (killed.size() <= id)
            return 0;
        else return killed.get(id);
    }
    
    public void setValue(int id, int amount) {
        while (id >= killed.size()) {
            killed.add(0);
        }
        killed.set(id, amount);
    }
    
    public void merge(QuestDataTaskMob other) {
        for (int i = 0; i < other.killed.size(); i++) {
            setValue(i, Math.max(this.getValue(i), other.getValue(i)));
        }
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.MOB;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, killed.size());
        builder.add(KILLED, Adapter.array(killed.toArray()).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.killed.clear();
        this.killed.addAll(((QuestDataTaskMob) taskData).killed);
    }
}
