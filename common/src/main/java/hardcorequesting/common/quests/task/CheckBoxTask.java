package hardcorequesting.common.quests.task;

import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class CheckBoxTask extends QuestTask<TaskData> {
    public CheckBoxTask(Quest parent) {
        super(TaskType.CHECKBOX, TaskData.class, parent);
    }

    @Override
    public TaskData newQuestData() {
        return new TaskData();
    }

    @Override
    public void onUpdate(Player player) {

    }

    @Override
    public float getCompletedRatio(Team team) {
        return getData(team).completed ? 1 : 0;
    }

    @Override
    public void mergeProgress(UUID id, TaskData own, TaskData other) {
        own.completed |= other.completed;

        if (own.completed) {
            completeTask(id);
        }
    }

    @Override
    protected void setComplete(TaskData data) {
        data.completed = true;
    }

    @Override
    public void write(Adapter.JsonObjectBuilder builder) {

    }

    @Override
    public void read(JsonObject object) {

    }
}
