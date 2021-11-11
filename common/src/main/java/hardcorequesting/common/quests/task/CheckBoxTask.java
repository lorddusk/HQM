package hardcorequesting.common.quests.task;

import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.CheckBoxTaskData;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class CheckBoxTask extends QuestTask<CheckBoxTaskData> {
    public CheckBoxTask(Quest parent) {
        super(TaskType.CHECKBOX, CheckBoxTaskData.class, parent);
    }

    @Override
    public CheckBoxTaskData newQuestData() {
        return new CheckBoxTaskData();
    }

    @Override
    public void onUpdate(Player player) {
        if (getData(player).isToggled()) {
            completeTask(player.getUUID());
        }
    }

    @Override
    public float getCompletedRatio(Team team) {
        return getData(team).completed ? 1 : 0;
    }

    @Override
    public void mergeProgress(UUID id, CheckBoxTaskData own, CheckBoxTaskData other) {
        own.merge(other);

        if (own.isToggled()) {
            completeTask(id);
        }
    }

    @Override
    protected void setComplete(CheckBoxTaskData data) {
        data.setToggled(true);
        data.completed = true;
    }

    @Override
    public void completeTask(UUID uuid) {
        setToggled(uuid, true);
        super.completeTask(uuid);
    }

    @Override
    public void write(Adapter.JsonObjectBuilder builder) {

    }

    @Override
    public void read(JsonObject object) {

    }
    
    public boolean isToggled(UUID id) {
        return getData(id).isToggled();
    }
    
    public void setToggled(UUID id, boolean toggled) {
        CheckBoxTaskData data = getData(id);
        data.setToggled(toggled | data.completed);
    }
}
