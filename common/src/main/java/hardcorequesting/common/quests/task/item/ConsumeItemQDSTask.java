package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.quests.Quest;

public class ConsumeItemQDSTask extends ConsumeItemTask {
    
    public ConsumeItemQDSTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    @Override
    public boolean allowManual() {
        return false;
    }
}
