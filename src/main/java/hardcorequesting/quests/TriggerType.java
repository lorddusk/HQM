package hardcorequesting.quests;

import hardcorequesting.Translator;
import hardcorequesting.client.interfaces.GuiColor;

public enum TriggerType {
    NONE("none", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            return true;
        }

        @Override
        public String getMessage(Quest quest) {
            return null;
        }
    },
    QUEST_TRIGGER("quest", false, true) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            return false;
        }

    },
    TASK_TRIGGER("task", true, true) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            if (quest.getTriggerTasks() >= quest.getTasks().size()) {
                return quest.isCompleted(playerName);
            } else {
                return quest.getTasks().get(quest.getTriggerTasks() - 1).isCompleted(playerName);
            }
        }

        @Override
        public String getMessage(Quest quest) {
            return super.getMessage(quest) + " (" + quest.getTriggerTasks() + ")";
        }
    },
    ANTI_TRIGGER("anti", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            return quest.isEnabled(playerName, false);
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
        return Translator.translate("hqm.trigger." + id + ".title");
    }

    public String getDescription() {
        return Translator.translate("hqm.trigger." + id + ".desc");
    }

    public boolean isUseTaskCount() {
        return useTaskCount;
    }

    public boolean doesWorkAsInvisible() {
        return workAsInvisible;
    }

    public abstract boolean isQuestVisible(Quest quest, String playerName);

    public String getMessage(Quest quest) {
        return GuiColor.ORANGE + getName();
    }
}
