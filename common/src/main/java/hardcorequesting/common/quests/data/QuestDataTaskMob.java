package hardcorequesting.common.quests.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskMob;
import net.minecraft.util.GsonHelper;

public class QuestDataTaskMob extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String KILLED = "killed";
    public int[] killed;
    
    public QuestDataTaskMob(QuestTask task) {
        super(task);
        this.killed = new int[((QuestTaskMob) task).elements.size()];
    }
    
    protected QuestDataTaskMob() {
        super();
        this.killed = new int[0];
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskMob data = new QuestDataTaskMob();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.killed = new int[GsonHelper.getAsInt(in, COUNT)];
        JsonArray array = GsonHelper.getAsJsonArray(in, KILLED);
        for (int i = 0; i < array.size(); i++) {
            data.killed[i] = array.get(i).getAsInt();
        }
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.MOB;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, killed.length);
        builder.add(KILLED, Adapter.array(killed).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.killed = ((QuestDataTaskMob) taskData).killed;
    }
}
