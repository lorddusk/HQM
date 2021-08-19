package hardcorequesting.common.quests.data;


import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

public class DeathTaskData extends TaskData {
    
    private static final String DEATHS = "deaths";
    public int deaths;
    
    public DeathTaskData() {
        super();
    }
    
    public static TaskData construct(JsonObject in) {
        DeathTaskData data = new DeathTaskData();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.deaths = GsonHelper.getAsInt(in, DEATHS);
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.DEATH;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(DEATHS, deaths);
    }
    
    @Override
    public void update(TaskData taskData) {
        super.update(taskData);
        this.deaths = ((DeathTaskData) taskData).deaths;
    }
}
