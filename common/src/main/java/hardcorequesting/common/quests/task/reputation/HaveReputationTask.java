package hardcorequesting.common.quests.task.reputation;

import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;

public class HaveReputationTask extends ReputationTask<TaskData> {
    
    public HaveReputationTask(Quest parent, String description, String longDescription) {
        super(TaskType.REPUTATION, TaskData.class, parent, description, longDescription);
        
        register(EventTrigger.Type.OPEN_BOOK, EventTrigger.Type.REPUTATION_CHANGE);
    }
    
    private void checkReputation(Player player) {
        if (parent.isEnabled(player) && parent.isAvailable(player) && this.isVisible(player.getUUID()) && !this.isCompleted(player)) {
            if (isPlayerInRange(player)) {
                completeTask(player.getUUID());
                parent.sendUpdatedDataToTeam(player);
            }
            
        }
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        int count = parts.size();
        if (count == 0) {
            return 0;
        }
        
        int valid = 0;
        for (Part setting : parts) {
            if (setting.isValid(team)) {
                valid++;
            }
        }
        
        return (float) valid / count;
    }
    
    @Override
    public TaskData newQuestData() {
        return new TaskData();
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public void onOpenBook(EventTrigger.BookOpeningEvent event) {
        checkReputation(event.getPlayer());
    }
    
    @Override
    public void onReputationChange(EventTrigger.ReputationEvent event) {
        checkReputation(event.getPlayer());
    }
}
