package hardcorequesting.common.quests.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class QuestDataTaskAdvancement extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String ADVANCED = "advanced";
    private final List<Boolean> advanced;
    
    public QuestDataTaskAdvancement(QuestTask task) {
        this(((GetAdvancementTask) task).elements.size());
    }
    
    protected QuestDataTaskAdvancement(int size) {
        super();
        this.advanced = new ArrayList<>(size);
        while (advanced.size() < size) {
            advanced.add(false);
        }
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskAdvancement data = new QuestDataTaskAdvancement(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, ADVANCED);
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getAsBoolean())
                data.complete(i);
        }
        return data;
    }
    
    public boolean getValue(int id) {
        if (advanced.size() <= id)
            return false;
        else return advanced.get(id);
    }
    
    public void complete(int id) {
        while (id >= advanced.size()) {
            advanced.add(false);
        }
        advanced.set(id, true);
    }
    
    public void clear() {
        advanced.clear();
    }
    
    public void mergeResult(QuestDataTaskAdvancement other) {
        for (int i = 0; i < other.advanced.size(); i++) {
            if (other.advanced.get(i))
                complete(i);
        }
    }
    
    public float getCompletedRatio(int size) {
        return (float) advanced.stream().limit(size).filter(Boolean::booleanValue).count() / size;
    }
    
    public boolean areAllCompleted(int size) {
        return advanced.stream().limit(size).allMatch(Boolean::booleanValue);
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.ADVANCEMENT;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, advanced.size());
        builder.add(ADVANCED, Adapter.array(advanced.toArray()).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.advanced.clear();
        this.advanced.addAll(((QuestDataTaskAdvancement) taskData).advanced);
    }
}

