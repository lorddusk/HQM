package hardcorequesting.fabric.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestTicker;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.UUID;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;

/**
 * When quests are visible, enabled and completed, and what completing them does.
 * <p>
 * The tests use players that are only an id, as the quest logic works on the player id.
 */
public class QuestLogicTests {
    private static final UUID PLAYER = id("player");

    private static Quest loaded(QuestLine line, String key) {
        Quest quest = line.questSetsManager.quests.get(id(key));
        check(quest != null, "The quest " + key + " was not loaded");
        return quest;
    }

    /**
     * Marks the quest as done without any of the effects of completing it.
     */
    private static void markCompleted(Quest quest, UUID player) {
        quest.getQuestData(player).completed = true;
    }

    private static void markTaskCompleted(Quest quest, int task, UUID player) {
        quest.getTasks().get(task).getData(player).completed = true;
    }

    private static JsonObject withTasks(JsonObject quest, int checkboxes) {
        JsonArray tasks = new JsonArray();
        for (int i = 0; i < checkboxes; i++)
            tasks.add(taskSamples().get("checkbox").deepCopy());
        quest.add("tasks", tasks);
        return quest;
    }

    private static Team squad(UUID owner, UUID member, RewardSetting setting) {
        Team squad = Team.single(owner);
        squad.setId(id("squad"));
        TeamManager.getInstance().addTeam(squad);
        squad.setName("Squad");
        squad.addPlayer(new PlayerEntry(member, true, false));
        squad.setRewardSetting(setting);
        QuestingDataManager.getInstance().getQuestingData(owner).setTeam(squad);
        QuestingDataManager.getInstance().getQuestingData(member).setTeam(squad);
        squad.clearProgress();   // makes room for both members in the rewards of the quests
        return squad;
    }

    // Availability

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questWithoutRequirementsIsOpen(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
            Quest a = loaded(line, "a");

            check(a.isVisible(PLAYER), "The quest should be visible");
            check(a.isEnabled(PLAYER), "The quest should be enabled");
            check(a.isAvailable(PLAYER), "The quest should be available");
            check(!a.isCompleted(PLAYER), "The quest should not be completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questNeedsItsPrerequisite(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), requiring(checkboxQuest("b"), "a")));
            Quest a = loaded(line, "a"), b = loaded(line, "b");

            check(b.isVisible(PLAYER), "A quest that is waiting for a visible quest should be visible");
            check(!b.isEnabled(PLAYER), "The quest should not be enabled before its prerequisite is completed");

            markCompleted(a, PLAYER);
            check(b.isEnabled(PLAYER), "The quest should be enabled when its prerequisite is completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void requiredParentCountIsRespected(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // Number of required parents, whether the quest is enabled after one parent, and after both
            int[][] cases = {{1, 1, 1}, {2, 0, 1}, {3, 0, 0}};   // 3 of 2 parents can never be met
            for (int[] expected : cases) {
                JsonObject child = requiring(checkboxQuest("c"), "a", "b");
                child.addProperty("parentrequirement", expected[0]);
                QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), checkboxQuest("b"), child));
                Quest a = loaded(line, "a"), b = loaded(line, "b"), c = loaded(line, "c");

                check(!c.isEnabled(PLAYER), "Requiring " + expected[0] + " of 2: enabled without any parent completed");
                markCompleted(a, PLAYER);
                equal(expected[1] == 1, c.isEnabled(PLAYER), "Requiring " + expected[0] + " of 2: enabled after one parent");
                markCompleted(b, PLAYER);
                equal(expected[2] == 1, c.isEnabled(PLAYER), "Requiring " + expected[0] + " of 2: enabled after both parents");
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void linkedQuestsExcludeEachOther(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(linking(checkboxQuest("a"), "b"), checkboxQuest("b")));
            Quest a = loaded(line, "a"), b = loaded(line, "b");

            check(a.isEnabled(PLAYER) && b.isEnabled(PLAYER), "Both linked quests should be open at first");

            markCompleted(b, PLAYER);
            check(!a.isLinkFree(PLAYER) && !a.isEnabled(PLAYER) && !a.isVisible(PLAYER), "The quest that is linked to a completed quest should be closed");

            line = t.loadBook(bookOf(linking(checkboxQuest("a"), "b"), checkboxQuest("b")));
            markCompleted(loaded(line, "a"), PLAYER);
            check(!loaded(line, "b").isEnabled(PLAYER), "The link works in both directions");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questTriggerHidesTheQuestAndItsChildren(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject hidden = checkboxQuest("a");
            hidden.addProperty("trigger", "QUEST_TRIGGER");
            QuestLine line = t.loadBook(bookOf(hidden, requiring(checkboxQuest("b"), "a")));
            Quest a = loaded(line, "a"), b = loaded(line, "b");

            check(!a.isVisible(PLAYER), "A quest trigger quest should not be visible");
            check(a.isEnabled(PLAYER), "A quest trigger quest can still be completed");
            check(!b.isVisible(PLAYER), "The child of a hidden quest should be hidden");

            markCompleted(a, PLAYER);
            check(b.isVisible(PLAYER), "The child should show up when the hidden quest is completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void taskTriggerShowsTheQuestAfterTheTasks(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject trigger = withTasks(quest("a", 0, 0), 3);
            trigger.addProperty("trigger", "TASK_TRIGGER");
            trigger.addProperty("triggertasks", 2);
            QuestLine line = t.loadBook(bookOf(trigger));
            Quest a = loaded(line, "a");

            check(!a.isVisible(PLAYER), "Hidden until the tasks are completed");
            markTaskCompleted(a, 0, PLAYER);
            check(!a.isVisible(PLAYER), "Still hidden after the first of two tasks");
            markTaskCompleted(a, 1, PLAYER);
            check(a.isVisible(PLAYER), "Visible after the second task");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void antiTriggerShowsTheQuestWhenItCouldBeCompleted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject anti = requiring(checkboxQuest("b"), "a");
            anti.addProperty("trigger", "ANTI_TRIGGER");
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), anti));
            Quest a = loaded(line, "a"), b = loaded(line, "b");

            check(!b.isVisible(PLAYER), "Hidden while the prerequisite is open");
            markCompleted(a, PLAYER);
            check(b.isVisible(PLAYER), "Visible when the prerequisite is completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void linkToMissingQuestDoesNotBreakAvailability(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(linking(checkboxQuest("a"), "b"), checkboxQuest("b")));
            Quest a = loaded(line, "a");
            line.questSetsManager.quests.remove(id("b"));   // gone without a being updated

            a.isEnabled(PLAYER);
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void requirementOfMissingQuestDoesNotBreakAddingRequirements(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), requiring(checkboxQuest("b"), "a"), checkboxQuest("c")));
            Quest b = loaded(line, "b");
            line.questSetsManager.quests.remove(id("a"));   // gone without b being updated

            b.addRequirement(id("c"));
            
            check(b.getRequirements().stream().anyMatch(quest -> quest != null && quest.getQuestId().equals(id("c"))), "The new requirement should be added");
        });
    }

    // Completing

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questIsCompletedWithItsLastTask(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(withTasks(quest("a", 0, 0), 2)));
            Quest a = loaded(line, "a");

            a.getTasks().get(0).completeTask(PLAYER);
            check(!a.isCompleted(PLAYER), "The quest should wait for the second task");

            a.getTasks().get(1).completeTask(PLAYER);
            check(a.isCompleted(PLAYER), "The quest should be completed with the second task");
            check(!a.isAvailable(PLAYER), "A completed quest that does not repeat is not available anymore");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void completingAQuestCompletesChildrenThatAreDone(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), requiring(checkboxQuest("b"), "a"), requiring(checkboxQuest("c"), "a")));
            Quest a = loaded(line, "a"), b = loaded(line, "b"), c = loaded(line, "c");
            markTaskCompleted(b, 0, PLAYER);   // b only waited for a

            a.getTasks().get(0).completeTask(PLAYER);

            check(b.isCompleted(PLAYER), "The child that had all its tasks done should be completed");
            check(!c.isCompleted(PLAYER), "The child that still has a task to do should not be completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void instantRepeatQuestStartsOverRightAway(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject repeating = checkboxQuest("a");
            JsonObject info = new JsonObject();
            info.addProperty("type", "INSTANT");
            repeating.add("repeat", info);
            QuestLine line = t.loadBook(bookOf(repeating));
            Quest a = loaded(line, "a");

            a.getTasks().get(0).completeTask(PLAYER);

            check(a.isCompleted(PLAYER), "The quest should be counted as completed");
            check(a.isAvailable(PLAYER), "The quest should be available again");
            check(!a.getTasks().get(0).isCompleted(PLAYER), "The progress of the task should be reset");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void rewardsAreUnlockedByTheRewardSetting(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            UUID owner = id("owner"), member = id("member");
            for (RewardSetting setting : RewardSetting.values()) {
                QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
                squad(owner, member, setting);
                Quest a = loaded(line, "a");

                a.getTasks().get(0).completeTask(owner);

                QuestData data = a.getQuestData(owner);
                int claimable = (data.canClaimPlayerReward(owner) ? 1 : 0) + (data.canClaimPlayerReward(member) ? 1 : 0);
                equal(setting == RewardSetting.RANDOM ? 1 : 2, claimable, "Members that can claim the reward with the setting " + setting);
            }
        });
    }

    // Repeating

    private static JsonObject repeating(String key, String type, int hours) {
        JsonObject quest = checkboxQuest(key);
        JsonObject info = new JsonObject();
        info.addProperty("type", type);
        info.addProperty("days", 0);
        info.addProperty("hours", hours);
        quest.add("repeat", info);
        return quest;
    }

    private static void finish(Quest quest, long time) {
        QuestData data = quest.getQuestData(PLAYER);
        data.completed = true;
        data.available = false;
        data.time = time;
        markTaskCompleted(quest, 0, PLAYER);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void intervalAndTimeQuestsRepeatWithTheGameTime(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(repeating("interval", "INTERVAL", 5), repeating("time", "TIME", 2), checkboxQuest("plain")));
            Quest interval = loaded(line, "interval"), time = loaded(line, "time"), plain = loaded(line, "plain");
            QuestingDataManager.getInstance().getQuestingData(PLAYER);
            finish(interval, 0);
            finish(time, 3);
            finish(plain, 0);

            ServerLevel level = helper.getLevel();
            ServerLevelData levelData = (ServerLevelData) level.getLevelData();
            long originalTime = levelData.getGameTime();
            QuestTicker ticker = new QuestTicker();
            try {
                levelData.setGameTime(4000);   // hour 4
                ticker.tick(level, false);
                check(!interval.isAvailable(PLAYER), "The interval quest repeats every 5 hours, not in hour 4");
                check(!time.isAvailable(PLAYER), "The time quest was completed in hour 3 and repeats after 2 hours, not in hour 4");

                levelData.setGameTime(5000);   // hour 5
                ticker.tick(level, false);
                check(interval.isAvailable(PLAYER), "The interval quest should repeat in hour 5");
                check(!interval.getTasks().get(0).isCompleted(PLAYER), "The progress of the interval quest should be reset");
                check(time.isAvailable(PLAYER), "The time quest should repeat in hour 5");
                check(!plain.isAvailable(PLAYER), "A quest that does not repeat should stay completed");
            } finally {
                levelData.setGameTime(originalTime);
            }
        });
    }
}
