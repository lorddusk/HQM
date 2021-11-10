package hardcorequesting.common.quests.data;

import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import net.minecraft.util.GsonHelper;

public class CheckBoxTaskData extends TaskData {
    private static final String TOGGLED = "toggled";
    private boolean toggled;

    public static TaskData construct(JsonObject in) {
        CheckBoxTaskData data = new CheckBoxTaskData();
        data.completed = GsonHelper.getAsBoolean(in, COMPLETED, false);
        data.toggled = GsonHelper.getAsBoolean(in, TOGGLED, false);
        return data;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void merge(CheckBoxTaskData data) {
        this.toggled |= data.toggled;
    }

    @Override
    public QuestTaskAdapter.QuestDataType getDataType() {
        return QuestTaskAdapter.QuestDataType.CHECKBOX;
    }

    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(TOGGLED, toggled);
    }

    @Override
    public void update(TaskData taskData) {
        super.update(taskData);
        this.toggled = ((CheckBoxTaskData) taskData).toggled;
    }
}
