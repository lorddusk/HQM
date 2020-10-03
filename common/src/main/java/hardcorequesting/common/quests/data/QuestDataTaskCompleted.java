package hardcorequesting.common.quests.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskCompleted;
import net.minecraft.util.GsonHelper;

public class QuestDataTaskCompleted extends QuestDataTask {
    private static final String COUNT = "count";
    private static final String QUESTS = "quests";
    public boolean[] quests;
    
    public QuestDataTaskCompleted(QuestTask task) {
        super(task);
        this.quests = new boolean[((QuestTaskCompleted) task).quests.length];
    }
    
    protected QuestDataTaskCompleted() {
        super();
        this.quests = new boolean[0];
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskCompleted data = new QuestDataTaskCompleted();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.quests = new boolean[GsonHelper.getAsInt(in, COUNT)];
        JsonArray array = GsonHelper.getAsJsonArray(in, QUESTS);
        for (int i = 0; i < array.size(); i++) {
            data.quests[i] = array.get(i).getAsBoolean();
        }
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.COMPLETED;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, quests.length);
        builder.add(QUESTS, Adapter.array(quests).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.quests = ((QuestDataTaskCompleted) taskData).quests;
    }
}

