package hardcorequesting.common.quests.task;

import hardcorequesting.common.quests.Quest;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 */
public abstract class IconQuestTask<T> extends QuestTask {
    public IconQuestTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
}
