package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.TaskType;

public class ConsumeItemQDSTask extends ConsumeItemTask {
    
    public ConsumeItemQDSTask(Quest parent) {
        super(TaskType.CONSUME_QDS.get(), parent);
    }
}
