package hardcorequesting.fabric.gametest;

import hardcorequesting.common.io.LocalDataManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestSet;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public abstract class HQMTestBase {
    
    protected void clearData() {
        // Initialize data with an empty data manager
        LocalDataManager dataManager = new LocalDataManager();
        QuestLine.reset().loadAll(dataManager, dataManager);
    }
    
    @SuppressWarnings("SameParameterValue")
    protected QuestSet createQuestSet(String name) {
        QuestSet set = new QuestSet(name, "A quest set used in testing");
        Quest.getQuestSets().add(set);
        return set;
    }
    
    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    protected Quest createQuest(String name, UUID id, QuestSet set) {
        Quest quest = new Quest(name, "A quest used in testing", 0, 0, false);
        quest.setId(id);
        quest.setQuestSet(set);
        return quest;
    }
    
    
    public Quest getQuest(UUID questId) {
        Quest quest = Quest.getQuest(questId);
        if (quest == null)
            throw new GameTestAssertException("Expected quest with id " + questId + " to exist");
        return quest;
    }
    
    public void assertQuestCompletionStatus(UUID questId, boolean status, Player player) {
        if (getQuest(questId).isCompleted(player) != status) {
            if (status)
                throw new GameTestAssertException("Expected quest with id " + questId + " to be completed");
            else throw new GameTestAssertException("Expected quest with id " + questId + " to be incomplete");
        }
    }
}