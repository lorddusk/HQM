package hardcorequesting.fabric.test;

import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.LifeSetting;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamAction;
import hardcorequesting.common.team.TeamManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.stream.StreamSupport;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;
import static hardcorequesting.fabric.test.TestTeams.invite;
import static hardcorequesting.fabric.test.TestTeams.player;
import static hardcorequesting.fabric.test.TestTeams.squad;
import static hardcorequesting.fabric.test.TestTeams.teamOf;

/**
 * Creating and managing teams through the team actions of the team screen, and what happens to the progress of the members.
 */
public class TeamTests {

    private static int namedTeams() {
        return (int) StreamSupport.stream(TeamManager.getInstance().getNamedTeams().spliterator(), false).count();
    }

    private static void join(ServerPlayer owner, ServerPlayer joining) {
        invite(owner, joining);
        TeamAction.ACCEPT.process(joining, teamOf(owner).getId().toString());
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void playersStartAloneAndCanCreateATeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper);
            check(teamOf(alice).isSingle(), "A new player should be alone");

            TeamAction.CREATE.process(alice, "Squad");

            Team team = teamOf(alice);
            check(!team.isSingle(), "Alice should be in a team");
            equal("Squad", team.getName(), "Team name");
            check(team.isOwner(alice), "The creator should be the owner");
            equal(1, team.getPlayerCount(), "Team size");
            equal(1, namedTeams(), "Number of teams");
            check(TeamManager.getInstance().getByTeamId(team.getId()) == team, "The team should be found by its id");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void teamNeedsAnUnusedName(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper), carol = player(helper);

            TeamAction.CREATE.process(alice, "Squad");
            TeamAction.CREATE.process(bob, "Squad");
            TeamAction.CREATE.process(carol, "");

            check(teamOf(bob).isSingle(), "A team can not take the name of another team");
            check(teamOf(carol).isSingle(), "A team needs a name");
            equal(1, namedTeams(), "Number of teams");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void invitedPlayerCanJoin(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            Team team = teamOf(alice);

            invite(alice, bob);
            PlayerEntry entry = team.getEntry(bob.getUUID());
            check(entry != null && !entry.isInTeam(), "Bob should be invited, but not a member yet");
            check(teamOf(bob).getInvites().contains(team), "Bob should see the invitation");
            equal(1, team.getPlayerCount(), "Team size before accepting");

            TeamAction.ACCEPT.process(bob, team.getId().toString());

            check(teamOf(bob) == team, "Bob should be in the team of Alice");
            equal(2, team.getPlayerCount(), "Team size after accepting");
            check(!team.isOwner(bob), "Bob is not the owner");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void invitedPlayerCanDecline(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            Team team = teamOf(alice);
            invite(alice, bob);

            TeamAction.DECLINE.process(bob, team.getId().toString());

            check(team.getEntry(bob.getUUID()) == null, "The invitation should be removed");
            check(teamOf(bob).isSingle(), "Bob should still be alone");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void onlyTheOwnerInvitesAndOnlyPlayersWithoutTeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper), carol = player(helper), dave = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);
            TeamAction.CREATE.process(dave, "Other squad");

            invite(bob, carol);
            check(teamOf(alice).getEntry(carol.getUUID()) == null, "A member that is not the owner can not invite");

            invite(alice, dave);
            check(teamOf(alice).getEntry(dave.getUUID()) == null, "A player that is in a team can not be invited");

            invite(carol, alice);
            check(teamOf(carol).getEntry(alice.getUUID()) == null, "A player without a team can not invite");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void ownerCanKickMembers(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper), carol = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);
            join(alice, carol);
            Team team = teamOf(alice);

            TeamAction.KICK.process(bob, carol.getUUID().toString());
            equal(3, team.getPlayerCount(), "A member can not kick");
            TeamAction.KICK.process(alice, alice.getUUID().toString());
            equal(3, team.getPlayerCount(), "The owner can not be kicked");

            TeamAction.KICK.process(alice, bob.getUUID().toString());
            equal(2, team.getPlayerCount(), "Team size after kicking");
            check(teamOf(bob).isSingle(), "The kicked player should be alone");
            check(teamOf(carol) == team, "The other member stays");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void membersCanLeaveButTheOwnerCannot(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);
            Team team = teamOf(alice);

            TeamAction.LEAVE.process(alice, "");
            check(teamOf(alice) == team && team.getPlayerCount() == 2, "The owner can not leave");

            TeamAction.LEAVE.process(bob, "");
            check(teamOf(bob).isSingle(), "Bob should be alone after leaving");
            equal(1, team.getPlayerCount(), "Team size after leaving");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void ownerCanDisbandTheTeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);

            TeamAction.DISBAND.process(bob, "");
            equal(1, namedTeams(), "A member can not disband the team");

            TeamAction.DISBAND.process(alice, "");
            equal(0, namedTeams(), "Number of teams after disbanding");
            check(teamOf(alice).isSingle() && teamOf(bob).isSingle(), "Everybody should be alone again");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void teamSettingsAreChangedByTheOwner(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);
            Team team = teamOf(alice);
            LifeSetting lives = team.getLifeSetting();
            RewardSetting rewards = team.getRewardSetting();

            TeamAction.NEXT_LIFE_SETTING.process(bob, "");
            TeamAction.NEXT_REWARD_SETTING.process(bob, "");
            check(team.getLifeSetting() == lives && team.getRewardSetting() == rewards, "A member can not change the settings");

            TeamAction.NEXT_LIFE_SETTING.process(alice, "");
            check(team.getLifeSetting() != lives, "The life setting should change");
            TeamAction.NEXT_LIFE_SETTING.process(alice, "");
            check(team.getLifeSetting() == lives, "The life setting should cycle through its values");

            // Reward setting: ALL, ANY and RANDOM are all used, and it is back at the start after going through them
            java.util.Set<RewardSetting> seen = new java.util.HashSet<>();
            for (int i = 0; i < 3; i++) {
                TeamAction.NEXT_REWARD_SETTING.process(alice, "");
                seen.add(team.getRewardSetting());
            }
            check(team.getRewardSetting() == rewards, "The reward setting should cycle through its values");
            check(seen.contains(RewardSetting.RANDOM) && seen.size() >= 2, "The reward setting should go through the values: " + seen);
        });
    }

    // Progress of the members

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void joiningMergesTheProgressOfThePlayer(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), checkboxQuest("b")));
            Quest a = line.questSetsManager.quests.get(id("a")), b = line.questSetsManager.quests.get(id("b"));
            ServerPlayer alice = player(helper), bob = player(helper);
            a.getTasks().get(0).completeTask(bob.getUUID());   // Bob did this on his own
            b.getTasks().get(0).completeTask(alice.getUUID());   // Alice did this
            TeamAction.CREATE.process(alice, "Squad");

            join(alice, bob);

            check(a.isCompleted(alice.getUUID()), "The team should have the quest that Bob completed");
            check(b.isCompleted(bob.getUUID()), "Bob should have the quest that the team completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void leavingKeepsTheProgressOfTheTeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), checkboxQuest("b")));
            Quest a = line.questSetsManager.quests.get(id("a")), b = line.questSetsManager.quests.get(id("b"));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);
            a.getTasks().get(0).completeTask(alice.getUUID());   // as a team

            TeamAction.LEAVE.process(bob, "");

            check(a.isCompleted(bob.getUUID()), "Bob should keep the quest the team completed");
            check(!b.isCompleted(bob.getUUID()), "Bob should not get quests the team did not complete");
            check(a.isCompleted(alice.getUUID()), "The team keeps its progress");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void joiningTakesTheHigherReputation(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper), carol = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            teamOf(alice).setReputation("rep1", -30);
            teamOf(alice).setReputation("rep2", 8);
            teamOf(bob).setReputation("rep1", 80);
            teamOf(bob).setReputation("rep2", 2);

            join(alice, bob);

            equal(80, teamOf(alice).getReputation("rep1"), "Reputation with the greater value");
            equal(8, teamOf(alice).getReputation("rep2"), "The team keeps the greater value");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void leavingTakesTheReputationOfTheTeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);
            TeamAction.CREATE.process(alice, "Squad");
            join(alice, bob);
            teamOf(alice).setReputation("rep1", 40);

            TeamAction.LEAVE.process(bob, "");

            equal(40, teamOf(bob).getReputation("rep1"), "Reputation of the player that left");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void disbandingWithAnOfflineMemberWorks(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper);
            UUID offline = id("offline_player");   // not part of the server, like a player that is not logged in
            QuestingDataManager.getInstance().getQuestingData(offline);
            squad("Squad", RewardSetting.ANY, alice.getUUID(), offline);
            
            TeamAction.DISBAND.process(alice, "");

            equal(0, namedTeams(), "Number of teams after disbanding");
            check(teamOf(alice).isSingle(), "The owner should be alone again");
            check(QuestingDataManager.getInstance().getQuestingData(offline).getTeam().isSingle(), "The offline member should be alone again");
        });
    }
}
