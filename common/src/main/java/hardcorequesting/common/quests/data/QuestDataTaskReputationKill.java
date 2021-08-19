package hardcorequesting.common.quests.data;


import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

public class QuestDataTaskReputationKill extends QuestDataTask {
    
    private static final String KILLS = "kills";
    public int kills;
    
    public QuestDataTaskReputationKill() {
        super();
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskReputationKill data = new QuestDataTaskReputationKill();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.kills = GsonHelper.getAsInt(in, KILLS);
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.REPUTATION_KILL;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(KILLS, kills);
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.kills = ((QuestDataTaskReputationKill) taskData).kills;
    }
}
