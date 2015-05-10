package hardcorequesting.quests;


import hardcorequesting.client.interfaces.GuiColor;

public enum TriggerType {
    NONE("Normal Quest", "Just a normal quest.", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            return true;
        }

        @Override
        public String getMessage(Quest quest) {
            return null;
        }
    },
    QUEST_TRIGGER("Trigger Quest", "A trigger quest is an invisible quest. The quest can still be completed as usual but you can't claim any rewards for it or see it in any lists. It can be used to trigger other quests, hence its name.", false, true) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            return false;
        }

    },
    TASK_TRIGGER("Trigger Tasks", "Trigger tasks are the first few tasks of a quest that have to be completed before the quest shows up. The quest will be invisible until the correct amount of tasks have been completed. When the quest becomes visible the player can see the tasks that have already been completed.", true, true) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            if (quest.getTriggerTasks() >= quest.getTasks().size()) {
                return quest.isCompleted(playerName);
            }else{
                return quest.getTasks().get(quest.getTriggerTasks() - 1).isCompleted(playerName);
            }
        }

        @Override
        public String getMessage(Quest quest) {
            return super.getMessage(quest) + " (" + quest.getTriggerTasks() + ")";
        }
    },
    ANTI_TRIGGER("Reversed Trigger", "This quest will be invisible until it is enabled (all its parent quests are completed). This way you can make a secret quest line appear all of a sudden when a known quest is completed.", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, String playerName) {
            return quest.isEnabled(playerName, false);
        }
    };

    private String name;
    private String description;
    private boolean useTaskCount;
    private boolean workAsInvisible;

    TriggerType(String name, String description, boolean useTaskCount, boolean workAsInvisible) {
        this.name = name;
        this.description = description;
        this.useTaskCount = useTaskCount;
        this.workAsInvisible = workAsInvisible;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
