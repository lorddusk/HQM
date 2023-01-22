package hardcorequesting.common.quests.data;


import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import net.minecraft.util.GsonHelper;

public class TaskData {
    
    protected static final String COMPLETED = "completed";
    public boolean completed;
    
    public TaskData() {
    }
    
    public static TaskData construct(JsonObject in) {
        TaskData data = new TaskData();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        return data;
    }
    
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(COMPLETED, completed);
    }
    
    public void update(TaskData taskData) {
        this.completed = taskData.completed;
    }
}
