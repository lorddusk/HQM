package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TameMobsTask;
import net.minecraft.util.GsonHelper;

public class QuestDataTaskTame extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String TAMED = "tamed";
    public int[] tamed;
    
    public QuestDataTaskTame(QuestTask task) {
        super(task);
        this.tamed = new int[((TameMobsTask) task).elements.size()];
    }
    
    protected QuestDataTaskTame() {
        super();
        this.tamed = new int[0];
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskTame data = new QuestDataTaskTame();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.tamed = new int[GsonHelper.getAsInt(in, COUNT)];
        JsonArray array = GsonHelper.getAsJsonArray(in, TAMED);
        for (int i = 0; i < array.size(); i++) {
            data.tamed[i] = array.get(i).getAsInt();
        }
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.TAME;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, tamed.length);
        builder.add(TAMED, Adapter.array(tamed).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.tamed = ((QuestDataTaskTame) taskData).tamed;
    }
}
