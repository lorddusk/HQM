package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class ItemsTaskData extends TaskData {
    
    private static final String COUNT = "count";
    private static final String PROGRESS = "progress";
    private final List<Integer> progress;
    
    public ItemsTaskData(int size) {
        super();
        this.progress = new ArrayList<>(size);
        while (progress.size() < size) {
            progress.add(0);
        }
    }
    
    public static TaskData construct(JsonObject in) {
        ItemsTaskData data = new ItemsTaskData(GsonHelper.getAsInt(in, COUNT));
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        JsonArray array = GsonHelper.getAsJsonArray(in, PROGRESS);
        for (int i = 0; i < array.size(); i++) {
            data.progress.set(i, array.get(i).getAsInt());
        }
        return data;
    }
    
    public int getValue(int index) {
        if (index >= progress.size()) {
            return 0;
        } else return progress.get(index);
    }
    
    public void setValue(int id, int amount) {
        while (id >= progress.size()) {
            progress.add(0);
        }
        progress.set(id, amount);
    }
    
    public void merge(ItemsTaskData other) {
        for (int i = 0; i < other.progress.size(); i++) {
            setValue(i, Math.max(this.getValue(i), other.getValue(i)));
        }
    }
    
    public boolean isDone(int index, ItemRequirementTask.Part requirement) {
        return this.getValue(index) >= requirement.required;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.ITEMS;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, progress.size());
        builder.add(PROGRESS, Adapter.array(progress.toArray()).build());
    }
    
    @Override
    public void update(TaskData taskData) {
        super.update(taskData);
        this.progress.clear();
        this.progress.addAll(((ItemsTaskData) taskData).progress);
    }
}
