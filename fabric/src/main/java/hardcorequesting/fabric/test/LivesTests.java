package hardcorequesting.fabric.test;

import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.LifeSetting;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;
import static hardcorequesting.fabric.test.TestTeams.player;
import static hardcorequesting.fabric.test.TestTeams.squad;

/**
 * Lives in hardcore mode: how they are lost and gained, alone and in a team, and the hearts.
 * <p>
 * The last life is not tested: losing it bans the player and kicks them from the server.
 */
public class LivesTests {
    private static int maxLives() {
        return HQMConfig.getInstance().Hardcore.MAX_LIVES;   // not a constant, the config is only available once the mod is loaded
    }

    private static QuestingDataManager hardcore() {
        QuestingDataManager manager = QuestingDataManager.getInstance();
        manager.activateHardcore();
        check(manager.isHardcoreActive(), "Hardcore mode could not be activated");
        return manager;
    }

    private static QuestingData lives(ServerPlayer player, int lives) {
        QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
        data.setRawLives(lives);
        return data;
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void addingLivesStopsAtTheMaximum(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer player = player(helper);
            QuestingData data = lives(player, maxLives() - 1);

            data.addLives(player, 5);

            equal(maxLives(), data.getRawLives(), "Lives after adding more than the maximum");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void aPlayerLosesOneLifePerDeath(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            QuestingData data = lives(player, 4);

            data.die(player);
            equal(4, data.getRawLives(), "Lives of a player dying outside of hardcore mode");

            hardcore();
            data.die(player);
            equal(3, data.getRawLives(), "Lives after dying in hardcore mode");
            check(data.removeLives(player, 1), "The player should be alive with two lives left");
            equal(2, data.getRawLives(), "Lives after losing one");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void sharedLivesAreTheSumOfTheMembers(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer alice = player(helper), bob = player(helper);
            lives(alice, 3);
            lives(bob, 5);
            Team team = squad("Squad", RewardSetting.ANY, alice.getUUID(), bob.getUUID());

            check(team.isSharingLives(), "Teams share their lives at first");
            QuestingData aliceData = QuestingDataManager.getInstance().getQuestingData(alice);
            equal(8, aliceData.getLives(), "Lives of Alice, shared with Bob");
            equal(3, aliceData.getRawLives(), "Own lives of Alice");
            equal(2, aliceData.getLivesToStayAlive(), "Lives the team needs to stay alive");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void sharedLivesAreTakenFromTheMemberFirst(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer alice = player(helper), bob = player(helper);
            QuestingData aliceData = lives(alice, 4), bobData = lives(bob, 5);
            squad("Squad", RewardSetting.ANY, alice.getUUID(), bob.getUUID());

            aliceData.removeLives(alice, 2);

            equal(2, aliceData.getRawLives(), "Lives of Alice");
            equal(5, bobData.getRawLives(), "Lives of Bob");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void sharedLivesAreTakenFromTeammatesWhenTheMemberHasNoSpare(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer alice = player(helper), bob = player(helper);
            QuestingData aliceData = lives(alice, 1), bobData = lives(bob, 5);
            squad("Squad", RewardSetting.ANY, alice.getUUID(), bob.getUUID());

            check(aliceData.removeLives(alice, 2), "The team still has lives left");

            equal(1, aliceData.getRawLives(), "A member keeps their last life as long as others have spare lives");
            equal(3, bobData.getRawLives(), "Lives of Bob");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void individualLivesIgnoreTheTeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer alice = player(helper), bob = player(helper);
            QuestingData aliceData = lives(alice, 4), bobData = lives(bob, 5);
            Team team = squad("Squad", RewardSetting.ANY, alice.getUUID(), bob.getUUID());
            team.setLifeSetting(LifeSetting.INDIVIDUAL);

            equal(4, aliceData.getLives(), "Lives of Alice");
            equal(1, aliceData.getLivesToStayAlive(), "Lives needed to stay alive");
            aliceData.removeLives(alice, 3);

            equal(1, aliceData.getRawLives(), "Lives of Alice");
            equal(5, bobData.getRawLives(), "Lives of Bob");
        });
    }

    // Hearts

    private static ItemStack useHeart(GameTestHelper helper, ServerPlayer player, Supplier<? extends Item> heart, int count) {
        player.setGameMode(GameType.SURVIVAL);   // hearts are not used up in creative mode
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(heart.get(), count));
        heart.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        return player.getItemInHand(InteractionHand.MAIN_HAND);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void heartAddsALife(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer player = player(helper);
            QuestingData data = lives(player, 3);

            ItemStack left = useHeart(helper, player, ModItems.heart, 2);

            equal(4, data.getRawLives(), "Lives after using a heart");
            equal(1, left.getCount(), "Hearts left");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void heartIsKeptAtTheMaximumAndOutsideOfHardcore(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            QuestingData data = lives(player, 3);

            ItemStack left = useHeart(helper, player, ModItems.heart, 2);
            equal(3, data.getRawLives(), "Lives after using a heart outside of hardcore mode");
            equal(2, left.getCount(), "Hearts left outside of hardcore mode");

            hardcore();
            data.setRawLives(maxLives());
            left = useHeart(helper, player, ModItems.heart, 2);
            equal(maxLives(), data.getRawLives(), "Lives after using a heart at the maximum");
            equal(2, left.getCount(), "Hearts left at the maximum");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void partialHeartsCombineIntoHearts(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Item quarter = ModItems.quarterHeart.get(), half = ModItems.halfHeart.get(), threeQuarters = ModItems.threeQuartsHeart.get(), full = ModItems.heart.get();
            Item[][] recipes = {
                    {full, half, half},
                    {full, quarter, quarter, half},
                    {full, quarter, quarter, quarter, quarter},
                    {full, quarter, threeQuarters},
                    {half, quarter, quarter},
                    {threeQuarters, quarter, half},
                    {threeQuarters, quarter, quarter, quarter},
            };
            for (Item[] recipe : recipes) {
                List<ItemStack> ingredients = new ArrayList<>();
                for (int i = 1; i < recipe.length; i++)
                    ingredients.add(new ItemStack(recipe[i]));

                ItemStack result = craft(helper, ingredients).orElse(ItemStack.EMPTY);
                check(result.is(recipe[0]), "Combining " + ingredients + " should give " + recipe[0] + " but gave " + result);
            }

            check(craft(helper, List.of(new ItemStack(half), new ItemStack(threeQuarters))).isEmpty(), "Half and three quarters are more than a heart");
            check(craft(helper, List.of(new ItemStack(quarter))).isEmpty(), "A quarter alone makes nothing");
            check(craft(helper, List.of(new ItemStack(threeQuarters), new ItemStack(threeQuarters))).isEmpty(), "Two threequarters make nothing");
        });
    }

    private static Optional<ItemStack> craft(GameTestHelper helper, List<ItemStack> ingredients) {
        List<ItemStack> grid = new ArrayList<>(ingredients);
        while (grid.size() < 9)
            grid.add(ItemStack.EMPTY);
        CraftingInput input = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        return recipe.map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
    }

    // Commands

    private static void run(GameTestHelper helper, ServerPlayer player, int permissionLevel, String command) {
        CommandSourceStack source = player.createCommandSourceStack().withPermission(permissionLevel);
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, command);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void livesCommandsChangeTheLives(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            hardcore();
            ServerPlayer player = player(helper);
            QuestingData data = lives(player, 3);

            run(helper, player, 0, "hqm lives add @s 2");
            equal(3, data.getRawLives(), "Lives after a command without permission");

            run(helper, player, 2, "hqm lives add @s 2");
            equal(5, data.getRawLives(), "Lives after adding two");

            run(helper, player, 2, "hqm lives remove @s 1");
            equal(4, data.getRawLives(), "Lives after removing one");

            run(helper, player, 2, "hqm lives add @s 1000");
            equal(maxLives(), data.getRawLives(), "Lives after adding more than the maximum");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void livesCommandsNeedHardcoreMode(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            QuestingData data = lives(player, 3);

            run(helper, player, 2, "hqm lives add @s 2");

            equal(3, data.getRawLives(), "Lives after a command outside of hardcore mode");
        });
    }
}
