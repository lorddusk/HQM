package hardcorequesting.fabric.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.team.RewardSetting;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;
import static hardcorequesting.fabric.test.TestTeams.player;
import static hardcorequesting.fabric.test.TestTeams.squad;
import static hardcorequesting.fabric.test.TestTeams.teamOf;

/**
 * Claiming the rewards of a quest, through the claim request of the quest screen.
 */
public class RewardTests {

    private static JsonObject rewardQuest(JsonArray rewards, JsonArray choices) {
        JsonObject quest = checkboxQuest("r");
        quest.add("reward", rewards);
        quest.add("rewardchoice", choices);
        return quest;
    }

    private static JsonArray stacks(Object... items) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < items.length; i += 2)
            array.add(stack((String) items[i], (Integer) items[i + 1]));
        return array;
    }

    private static JsonArray reputationReward(int value) {
        JsonArray array = new JsonArray();
        JsonObject reward = new JsonObject();
        reward.addProperty("reputation", "rep1");
        reward.addProperty("value", value);
        array.add(reward);
        return array;
    }

    private static void complete(Quest quest, ServerPlayer player) {
        quest.getTasks().get(0).completeTask(player.getUUID());
    }

    private static void claim(Quest quest, ServerPlayer player, int choice) {
        JsonObject request = new JsonObject();
        request.addProperty("quest", quest.getQuestId().toString());
        request.addProperty("reward", choice);
        ClientChange.CLAIM_QUEST.parse(player, request.toString());
    }

    private static int count(ServerPlayer player, Item item) {
        return player.getInventory().countItem(item);
    }

    private static Quest loaded(QuestLine line) {
        return line.questSetsManager.quests.get(id("r"));
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void itemRewardsAreMergedAndClaimedOnce(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Quest quest = loaded(t.loadBook(bookOf(rewardQuest(stacks("minecraft:diamond", 3, "minecraft:diamond", 2), new JsonArray()))));
            ServerPlayer player = player(helper);

            claim(quest, player, -1);
            equal(0, count(player, Items.DIAMOND), "Diamonds claimed before the quest is completed");

            complete(quest, player);
            check(quest.hasReward(player.getUUID()), "The completed quest should have a reward to claim");
            claim(quest, player, -1);
            equal(5, count(player, Items.DIAMOND), "Diamonds after claiming");
            check(!quest.hasReward(player.getUUID()), "There should be nothing left to claim");

            claim(quest, player, -1);
            equal(5, count(player, Items.DIAMOND), "Diamonds after claiming again");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void fullInventoryKeepsTheReward(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Quest quest = loaded(t.loadBook(bookOf(rewardQuest(stacks("minecraft:diamond", 3), new JsonArray()))));
            ServerPlayer player = player(helper);
            complete(quest, player);
            for (int i = 0; i < player.getInventory().items.size(); i++)
                player.getInventory().items.set(i, new ItemStack(Items.STONE_SWORD));

            claim(quest, player, -1);
            equal(0, count(player, Items.DIAMOND), "Diamonds claimed with a full inventory");
            check(quest.hasReward(player.getUUID()), "The reward should stay claimable");

            player.getInventory().items.set(0, ItemStack.EMPTY);
            claim(quest, player, -1);
            equal(3, count(player, Items.DIAMOND), "Diamonds claimed after making room");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void choiceRewardNeedsAValidChoice(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Quest quest = loaded(t.loadBook(bookOf(rewardQuest(stacks("minecraft:diamond", 1),
                    stacks("minecraft:iron_pickaxe", 1, "minecraft:iron_axe", 1)))));
            ServerPlayer player = player(helper);
            complete(quest, player);

            claim(quest, player, -1);
            claim(quest, player, 2);
            equal(0, count(player, Items.DIAMOND), "Diamonds claimed without a valid choice");
            check(quest.hasReward(player.getUUID()), "The reward should stay claimable");

            claim(quest, player, 1);
            equal(1, count(player, Items.IRON_AXE), "The chosen item");
            equal(0, count(player, Items.IRON_PICKAXE), "The item that was not chosen");
            equal(1, count(player, Items.DIAMOND), "The fixed reward comes with the choice");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void reputationRewardIsGivenOnce(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject quest = rewardQuest(new JsonArray(), new JsonArray());
            quest.add("reputationrewards", reputationReward(50));
            Quest loaded = loaded(t.loadBook(bookOf(quest)));
            ServerPlayer player = player(helper);
            complete(loaded, player);

            claim(loaded, player, -1);
            equal(50, teamOf(player).getReputation("rep1"), "Reputation after claiming");
            claim(loaded, player, -1);
            equal(50, teamOf(player).getReputation("rep1"), "Reputation after claiming again");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void commandRewardsRunWithPermissionLevel2(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject quest = rewardQuest(new JsonArray(), new JsonArray());
            JsonArray commands = new JsonArray();
            commands.add("tag @s add hqm_reward_test");
            commands.add("setidletimeout 7");   // needs permission level 3
            quest.add("commandrewards", commands);
            Quest loaded = loaded(t.loadBook(bookOf(quest)));
            ServerPlayer player = player(helper);
            MinecraftServer server = helper.getLevel().getServer();
            int idleTimeout = server.getPlayerIdleTimeout();
            try {
                complete(loaded, player);
                claim(loaded, player, -1);

                check(player.getTags().contains("hqm_reward_test"), "The command of the reward should have been run for the player");
                check(server.getPlayerIdleTimeout() != 7, "Command rewards must not run commands that need a higher permission level");

                player.removeTag("hqm_reward_test");
                claim(loaded, player, -1);
                check(!player.getTags().contains("hqm_reward_test"), "The command should not run again");
            } finally {
                server.setPlayerIdleTimeout(idleTimeout);
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void teamsClaimRewardsByTheRewardSetting(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            for (RewardSetting setting : RewardSetting.values()) {
                JsonObject quest = rewardQuest(stacks("minecraft:diamond", 1), new JsonArray());
                quest.add("reputationrewards", reputationReward(50));
                Quest loaded = loaded(t.loadBook(bookOf(quest)));
                ServerPlayer alice = player(helper), bob = player(helper);
                squad("Squad", setting, alice.getUUID(), bob.getUUID());
                complete(loaded, alice);

                claim(loaded, alice, -1);
                claim(loaded, bob, -1);

                // ALL gives every member the items, ANY gives them to one member, RANDOM to the one that was picked
                int diamonds = count(alice, Items.DIAMOND) + count(bob, Items.DIAMOND);
                equal(setting == RewardSetting.ALL ? 2 : 1, diamonds, "Diamonds given to the team with the setting " + setting);
                equal(50, teamOf(alice).getReputation("rep1"), "The team gets the reputation once with the setting " + setting);
            }
        });
    }
}
