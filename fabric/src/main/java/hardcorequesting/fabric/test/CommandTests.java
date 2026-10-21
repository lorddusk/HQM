package hardcorequesting.fabric.test;

import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.fabric.test.TestCommands.Result;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestCommands.run;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;
import static hardcorequesting.fabric.test.TestTeams.player;

/**
 * The /hqm commands: who may run them and what they do.
 */
public class CommandTests {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void versionIsOpenToEveryone(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);

            Result result = run(player, 0, "hqm version");

            check(result.ok(), "The version command should work without permission: " + result.error());
            check(result.said("hqm.message.version"), "The version should be told");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void commandsNeedTheirPermissionLevel(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            QuestingDataManager.getInstance().activateHardcore();   // the lives commands need it
            ServerPlayer player = player(helper);

            Object[][] commands = {
                    {"hqm enable", 2}, {"hqm quest", 2}, {"hqm hardcore", 2}, {"hqm hardcore enable", 2},
                    {"hqm lives add @s 1", 2}, {"hqm lives remove @s 1", 2}, {"hqm hardcore disable", 2}, {"hqm op", 2},
                    {"hqm reset @s yes-this-is-really-what-i-want", 2}, {"hqm edit", 4},
            };
            for (Object[] command : commands) {
                String text = (String) command[0];
                int level = (Integer) command[1];

                Result tooLow = run(player, level - 1, text);
                check(!tooLow.ok(), "'" + text + "' should not work with permission level " + (level - 1));

                Result enough = run(player, level, text);
                check(enough.ok(), "'" + text + "' should work with permission level " + level + ": " + enough.error());
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void unknownSubcommandsAreRejected(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);

            check(!run(player, 4, "hqm nothing").ok(), "An unknown subcommand should fail");
            check(!run(player, 4, "hqm lives add").ok(), "A command with missing arguments should fail");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void enableActivatesQuestingAndHardcore(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            QuestingDataManager manager = QuestingDataManager.getInstance();
            manager.deactivate();   // questing mode is on in a new book, unless the config says otherwise
            ServerPlayer player = player(helper);
            check(!manager.isQuestActive() && !manager.isHardcoreActive(), "Both modes should be off");

            Result first = run(player, 2, "hqm enable");
            check(manager.isQuestActive() && manager.isHardcoreActive(), "Both modes should be on");
            check(first.said("hqm.message.questHardcore"), "The first time it should tell that the modes are activated");

            Result second = run(player, 2, "hqm enable");
            check(second.said("hqm.message.hardcoreAlreadyActivated"), "The second time it should tell that hardcore mode was on already");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void hardcoreCanBeSwitchedOnAndOff(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            QuestingDataManager manager = QuestingDataManager.getInstance();

            run(player, 2, "hqm hardcore");
            check(manager.isHardcoreActive(), "'hardcore' should switch hardcore mode on");

            Result off = run(player, 2, "hqm hardcore disable");
            check(!manager.isHardcoreActive(), "'hardcore disable' should switch hardcore mode off");
            check(off.said("hqm.message.hardcoreDisabled"), "It should tell that hardcore mode is disabled");

            run(player, 2, "hqm hardcore enable");
            check(manager.isHardcoreActive(), "'hardcore enable' should switch hardcore mode on");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questCommandActivatesQuesting(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            QuestingDataManager.getInstance().deactivate();   // questing mode is on in a new book, unless the config says otherwise
            ServerPlayer player = player(helper);

            Result first = run(player, 2, "hqm quest");
            check(first.said("hqm.message.questActivated"), "The first time it should tell that questing is activated, but said " + first.described());
            check(QuestingDataManager.getInstance().isQuestActive(), "Questing mode should be on");
            check(run(player, 2, "hqm quest").said("hqm.message.questAlreadyActivated"), "The second time it should tell that it was activated already");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void editTogglesTheEditMode(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            Quest.setEditMode(false);

            check(run(player, 4, "hqm edit").said("hqm.command.editMode.enabled"), "It should tell that edit mode is enabled");
            check(Quest.canQuestsBeEdited(), "Edit mode should be on");
            check(run(player, 4, "hqm edit").said("hqm.command.editMode.disabled"), "It should tell that edit mode is disabled");
            check(!Quest.canQuestsBeEdited(), "Edit mode should be off");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void resetNeedsTheConfirmationAndRemovesTheProgress(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
            Quest quest = line.questSetsManager.quests.get(id("a"));
            ServerPlayer player = player(helper);
            QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
            data.setRawLives(7);
            quest.getTasks().get(0).completeTask(player.getUUID());

            check(!run(player, 2, "hqm reset @s").ok(), "Resetting needs the confirmation");
            check(quest.isCompleted(player.getUUID()), "The progress is kept without the confirmation");

            Result result = run(player, 2, "hqm reset @s yes-this-is-really-what-i-want");
            check(result.ok(), "Resetting should work: " + result.error());
            check(!quest.isCompleted(player.getUUID()), "The progress should be removed");
            check(QuestingDataManager.getInstance().getQuestingData(player).getRawLives() != 7, "The lives should be reset");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void opCommandGivesTheOpBook(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            equal(0, player.getInventory().countItem(ModItems.enabledBook.get()), "OP books before");

            run(player, 2, "hqm op");

            equal(1, player.getInventory().countItem(ModItems.enabledBook.get()), "OP books after");
        });
    }
}
