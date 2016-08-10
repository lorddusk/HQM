package hardcorequesting.quests.task;

import hardcorequesting.quests.Quest;

public class QuestTaskItemsConsumeQDS extends QuestTaskItemsConsume {
    public QuestTaskItemsConsumeQDS(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }

    @Override
    public boolean allowManual() {
        return false;
    }
}
