package hardcorequesting.common.quests.task;

import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import net.minecraft.world.entity.player.Player;

public class HaveReputationTask extends ReputationTask {
    
    public HaveReputationTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription, 0);
        
        register(EventTrigger.Type.OPEN_BOOK, EventTrigger.Type.REPUTATION_CHANGE);
    }
    
    private void checkReputation(Player player) {
        if (parent.isEnabled(player) && parent.isAvailable(player) && this.isVisible(player) && !this.isCompleted(player)) {
            if (isPlayerInRange(player)) {
                completeTask(player.getUUID());
                parent.sendUpdatedDataToTeam(player);
            }
            
        }
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
