package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;

public class QuestDataTaskItems extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String PROGRESS = "progress";
    public int[] progress;
    
    public QuestDataTaskItems(QuestTask task) {
        super(task);
        this.progress = new int[((ItemRequirementTask) task).getItems().size()];
    }
    
    protected QuestDataTaskItems() {
        super();
        this.progress = new int[0];
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskItems data = new QuestDataTaskItems();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.progress = new int[GsonHelper.getAsInt(in, COUNT)];
        JsonArray array = GsonHelper.getAsJsonArray(in, PROGRESS);
        for (int i = 0; i < array.size(); i++) {
            data.progress[i] = array.get(i).getAsInt();
        }
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.ITEMS;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, progress.length);
        builder.add(PROGRESS, Adapter.array(progress).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.progress = ((QuestDataTaskItems) taskData).progress;
    }
    
    public int getProgressFor(int index) {
        if (this.progress.length > index) {
            return this.progress[index];
        }
        this.progress = Arrays.copyOf(this.progress, index); // We need to be sure that the progress per item has the same length as the required items
        return 0;
    }
    
    public boolean isDone(int index, ItemRequirementTask.ItemRequirement requirement) {
        return this.getProgressFor(index) >= requirement.required;
    }
}
