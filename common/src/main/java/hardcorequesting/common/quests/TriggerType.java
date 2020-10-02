package hardcorequesting.common.quests;

import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.util.Translator;

import java.util.UUID;

public enum TriggerType {
    NONE("none", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            return true;
        }
        
        @Override
        public String getMessage(Quest quest) {
            return null;
        }
    },
    QUEST_TRIGGER("quest", false, true) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            return false;
        }
        
    },
    TASK_TRIGGER("task", true, true) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            if (quest.getTriggerTasks() >= quest.getTasks().size()) {
                return quest.isCompleted(playerId);
            } else {
                return quest.getTasks().get(quest.getTriggerTasks() - 1).isCompleted(playerId);
            }
        }
        
        @Override
        public String getMessage(Quest quest) {
            return super.getMessage(quest) + " (" + quest.getTriggerTasks() + ")";
        }
    },
    ANTI_TRIGGER("anti", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            return quest.isEnabled(playerId, false);
        }
    };
    
    private String id;
    private boolean useTaskCount;
    private boolean workAsInvisible;
    
    TriggerType(String id, boolean useTaskCount, boolean workAsInvisible) {
        this.id = id;
        this.useTaskCount = useTaskCount;
        this.workAsInvisible = workAsInvisible;
    }
    
    public String getName() {
        return Translator.get("hqm.trigger." + id + ".title");
    }
    
    public String getDescription() {
        return Translator.get("hqm.trigger." + id + ".desc");
    }
    
    public boolean isUseTaskCount() {
        return useTaskCount;
    }
    
    public boolean doesWorkAsInvisible() {
        return workAsInvisible;
    }
    
    public abstract boolean isQuestVisible(Quest quest, UUID playerId);
    
    public String getMessage(Quest quest) {
        return GuiColor.ORANGE + getName();
    }
}
