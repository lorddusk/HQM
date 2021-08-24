package hardcorequesting.common.quests.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class CompleteQuestTaskData extends TaskData {
    private static final String COUNT = "count";
    private static final String QUESTS = "quests";
    private final List<Boolean> quests;
    
    public CompleteQuestTaskData(int size) {
        super();
        this.quests = new ArrayList<>(size);
        while (quests.size() < size) {
            quests.add(false);
        }
    }
    
    public static TaskData construct(JsonObject in) {
        CompleteQuestTaskData data = new CompleteQuestTaskData(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, QUESTS);
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).getAsBoolean())
                data.complete(i);
        }
        return data;
    }
    
    public boolean getValue(int id) {
        if (id >= quests.size())
            return false;
        else return quests.get(id);
    }
    
    public void complete(int id) {
        while (id >= quests.size()) {
            quests.add(false);
        }
        quests.set(id, true);
    }
    
    public void mergeResult(CompleteQuestTaskData other) {
        for (int i = 0; i < other.quests.size(); i++) {
            if (other.quests.get(i))
                complete(i);
        }
    }
    
    public float getCompletedRatio(int size) {
        return (float) quests.stream().limit(size).filter(Boolean::booleanValue).count() / size;
    }
    
    public boolean areAllCompleted(int size) {
        return quests.stream().limit(size).allMatch(Boolean::booleanValue);
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.COMPLETED;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, quests.size());
        builder.add(QUESTS, Adapter.array(quests.toArray()).build());
    }
    
    @Override
    public void update(TaskData taskData) {
        super.update(taskData);
        this.quests.clear();
        this.quests.addAll(((CompleteQuestTaskData) taskData).quests);
    }
}

