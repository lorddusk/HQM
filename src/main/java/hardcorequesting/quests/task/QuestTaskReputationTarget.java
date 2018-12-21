package hardcorequesting.quests.task;

import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import net.minecraft.entity.player.EntityPlayer;

public class QuestTaskReputationTarget extends QuestTaskReputation {

    public QuestTaskReputationTarget(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription, 0);

        register(EventTrigger.Type.OPEN_BOOK, EventTrigger.Type.REPUTATION_CHANGE);
    }

    private void checkReputation(EntityPlayer player) {
        if (parent.isEnabled(player) && parent.isAvailable(player) && this.isVisible(player) && !this.isCompleted(player)) {
            if (isPlayerInRange(player)) {
                completeTask(player.getUniqueID());
                parent.sendUpdatedDataToTeam(player);
            }

        }
    }

    @Override
    public void onUpdate(EntityPlayer player) {

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
