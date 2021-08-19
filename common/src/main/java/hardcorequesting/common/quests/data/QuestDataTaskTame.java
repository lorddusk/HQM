package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class QuestDataTaskTame extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String TAMED = "tamed";
    private final List<Integer> tamed;
    
    public QuestDataTaskTame(int size) {
        super();
        this.tamed = new ArrayList<>(size);
        while (tamed.size() < size) {
            tamed.add(0);
        }
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskTame data = new QuestDataTaskTame(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, TAMED);
        for (int i = 0; i < array.size(); i++) {
            data.setValue(i, array.get(i).getAsInt());
        }
        return data;
    }
    
    public int getValue(int id) {
        if (tamed.size() <= id)
            return 0;
        else return tamed.get(id);
    }
    
    public void setValue(int id, int amount) {
        while (id >= tamed.size()) {
            tamed.add(0);
        }
        tamed.set(id, amount);
    }
    
    public void merge(QuestDataTaskTame other) {
        for (int i = 0; i < other.tamed.size(); i++) {
            setValue(i, Math.max(this.getValue(i), other.getValue(i)));
        }
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.TAME;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, tamed.size());
        builder.add(TAMED, Adapter.array(tamed.toArray()).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.tamed.clear();
        this.tamed.addAll(((QuestDataTaskTame) taskData).tamed);
    }
}
