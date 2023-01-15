package hardcorequesting.common.quests.data;


import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import net.minecraft.util.GsonHelper;

public class DeathTaskData extends TaskData {
    
    private static final String DEATHS = "deaths";
    private int deaths;
    
    public DeathTaskData() {
        super();
    }
    
    public static DeathTaskData construct(JsonObject in) {
        DeathTaskData data = new DeathTaskData();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.deaths = GsonHelper.getAsInt(in, DEATHS);
        return data;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public void merge(DeathTaskData data) {
        deaths = Math.max(this.deaths, data.deaths);
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
