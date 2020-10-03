package hardcorequesting.common.quests.data;


import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import net.minecraft.util.GsonHelper;

public class QuestDataTask {
    
    protected static final String COMPLETED = "completed";
    public boolean completed;
    
    public QuestDataTask(QuestTask task) {
    }
    
    protected QuestDataTask() {
        
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTask data = new QuestDataTask();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        return data;
    }
    
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.GENERIC;
    }
    
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(COMPLETED, completed);
    }
    
    public void update(QuestDataTask taskData) {
        this.completed = taskData.completed;
    }
}
