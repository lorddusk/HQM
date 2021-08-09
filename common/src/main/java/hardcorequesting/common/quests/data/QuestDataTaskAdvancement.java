package hardcorequesting.common.quests.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskAdvancement;
import net.minecraft.util.GsonHelper;

public class QuestDataTaskAdvancement extends QuestDataTask {
    
    private static final String COUNT = "count";
    private static final String ADVANCED = "advanced";
    public boolean[] advanced;
    
    public QuestDataTaskAdvancement(QuestTask task) {
        super(task);
        this.advanced = new boolean[((QuestTaskAdvancement) task).elements.size()];
    }
    
    protected QuestDataTaskAdvancement() {
        super();
        this.advanced = new boolean[0];
    }
    
    public static QuestDataTask construct(JsonObject in) {
        QuestDataTaskAdvancement data = new QuestDataTaskAdvancement();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.advanced = new boolean[GsonHelper.getAsInt(in, COUNT)];
        JsonArray array = GsonHelper.getAsJsonArray(in, ADVANCED);
        for (int i = 0; i < array.size(); i++) {
            data.advanced[i] = array.get(i).getAsBoolean();
        }
        return data;
    }
    
    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.ADVANCEMENT;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(COUNT, advanced.length);
        builder.add(ADVANCED, Adapter.array(advanced).build());
    }
    
    @Override
    public void update(QuestDataTask taskData) {
        super.update(taskData);
        this.advanced = ((QuestDataTaskAdvancement) taskData).advanced;
    }
}

