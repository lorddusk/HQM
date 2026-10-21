package hardcorequesting.fabric.test;

import com.google.gson.JsonObject;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;

/**
 * Progress and completion of the tasks, for every type of task, through the events of the game where possible.
 */
public class TaskCompletionTests {
    /**
     * The task types that have a test in this class. A new task type has to get one.
     */
    private static final Set<String> TESTED = Set.of("checkbox", "consume", "craft", "location", "consume_qds", "detect", "kill", "tame", "death",
            "reputation", "reputation_kill", "advancement", "completion", "block_break", "block_place");

    /**
     * A quest with the task that is tested, and a player that works on it.
     */
    private record Play(QuestLine line, Quest quest, QuestTask<?> task, ServerPlayer player) {
        UUID id() {
            return player.getUUID();
        }

        Team team() {
            return QuestingDataManager.getInstance().getQuestingData(player).getTeam();
        }

        float ratio() {
            return task.getCompletedRatio(team());
        }

        void expectRatio(float expected, String message) {
            check(Math.abs(ratio() - expected) < 0.01F, message + ": expected progress " + expected + " but was " + ratio());
        }
    }

    private static Play play(TestScope t, JsonObject task, JsonObject... otherQuests) {
        JsonObject[] quests = new JsonObject[otherQuests.length + 1];
        quests[0] = questWithTask("q", 0, 0, task);
        System.arraycopy(otherQuests, 0, quests, 1, otherQuests.length);
        QuestLine line = t.loadBook(bookOf(quests));
        Quest quest = line.questSetsManager.quests.get(id("q"));
        ServerPlayer player = t.helper().makeMockServerPlayerInLevel();
        player.setPos(t.helper().absoluteVec(new Vec3(1.5, 1, 1.5)));
        return new Play(line, quest, quest.getTasks().get(0), player);
    }

    private static JsonObject items(String type, String list, String key) {
        return json("{\"type\": \"hardcorequesting:" + type + "\", \"" + key + "\": [" + list + "]}");
    }

    private static String part(String item, int required) {
        return "{\"item\": {\"id\": \"" + item + "\", \"Count\": 1}, \"required\": " + required + "}";
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void everyTaskTypeHasACompletionTest(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Set<String> registered = TaskType.values().stream().map(type -> type.toDataName().replace("hardcorequesting:", "")).collect(Collectors.toSet());
            for (String type : registered)
                check(TESTED.contains(type), "The task type " + type + " has no completion test. Add one to TaskCompletionTests and list it in TESTED.");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void checkboxIsCompletedByTheClient(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, taskSamples().get("checkbox"));

            JsonObject request = new JsonObject();
            request.addProperty("quest", play.quest().getQuestId().toString());
            request.addProperty("task", 0);
            ClientChange.COMPLETE_CHECKBOX_TASK.parse(play.player(), request.toString());

            check(play.task().isCompleted(play.id()), "The checkbox task should be completed");
            check(play.quest().isCompleted(play.id()), "The quest should be completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void consumeTakesItemsFromTheInventory(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("consume", part("minecraft:bread", 5), "items"));

            play.player().getInventory().add(new ItemStack(Items.BREAD, 3));
            play.task().onUpdate(play.player());
            play.expectRatio(0.6F, "After 3 of 5");
            equal(0, play.player().getInventory().countItem(Items.BREAD), "Bread left in the inventory");
            check(!play.task().isCompleted(play.id()), "Not completed with 3 of 5");

            play.player().getInventory().add(new ItemStack(Items.BREAD, 4));
            play.task().onUpdate(play.player());
            check(play.task().isCompleted(play.id()), "Completed with 7 of 5");
            equal(2, play.player().getInventory().countItem(Items.BREAD), "The bread that was not needed stays in the inventory");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void consumeIgnoresOtherItems(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("consume", part("minecraft:bread", 5), "items"));

            play.player().getInventory().add(new ItemStack(Items.APPLE, 9));
            play.task().onUpdate(play.player());

            play.expectRatio(0, "With only apples");
            equal(9, play.player().getInventory().countItem(Items.APPLE), "Apples left in the inventory");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void craftingCountsCraftedItems(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("craft", part("minecraft:stick", 8), "items"));

            EventTrigger.instance().onCrafting(play.player(), new ItemStack(Items.STICK, 5));
            play.expectRatio(5 / 8F, "After crafting 5 of 8");
            EventTrigger.instance().onCrafting(play.player(), new ItemStack(Items.TORCH, 20));
            play.expectRatio(5 / 8F, "Crafting something else");
            EventTrigger.instance().onCrafting(play.player(), new ItemStack(Items.STICK, 3));
            check(play.task().isCompleted(play.id()), "Completed after crafting 8");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void detectCountsItemsWithoutTakingThem(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("detect", part("minecraft:iron_ingot", 16) + "," + part("minecraft:diamond", 1), "items"));

            play.player().getInventory().add(new ItemStack(Items.IRON_INGOT, 16));
            play.task().onUpdate(play.player());
            play.expectRatio(16 / 17F, "With the iron but no diamond");   // the progress counts the required items of all parts

            play.player().getInventory().add(new ItemStack(Items.DIAMOND));
            play.task().onUpdate(play.player());
            check(play.task().isCompleted(play.id()), "Completed with all items");
            equal(16, play.player().getInventory().countItem(Items.IRON_INGOT), "The items are only counted, not taken");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void detectCountsPickedUpItems(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("detect", part("minecraft:iron_ingot", 2), "items"));

            ItemEntity item = new ItemEntity(helper.getLevel(), play.player().getX(), play.player().getY(), play.player().getZ(), new ItemStack(Items.IRON_INGOT, 2));
            item.setNoPickUpDelay();
            helper.getLevel().addFreshEntity(item);
            item.playerTouch(play.player());

            check(play.task().isCompleted(play.id()), "Picking up the items should complete the task");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void deliverySystemTakesItemsForItsTask(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("consume_qds", part("minecraft:cobblestone", 5), "items"));
            BlockPos pos = new BlockPos(3, 1, 3);
            helper.setBlock(pos, ModBlocks.blockBarrel.get());
            AbstractBarrelBlockEntity barrel = (AbstractBarrelBlockEntity) helper.getBlockEntity(pos);

            // Binding works with the quest book, for the quest and task the player has selected
            QuestingData data = QuestingDataManager.getInstance().getQuestingData(play.player());
            data.selectedQuestId = play.quest().getQuestId();
            data.selectedTask = 0;
            play.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.book.get()));
            helper.getLevel().getBlockState(helper.absolutePos(pos)).useItemOn(play.player().getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), play.player(),
                    InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(pos)), Direction.UP, helper.absolutePos(pos), false));
            check(barrel.getCurrentTask() == play.task(), "The delivery system should be bound to the selected task");

            ItemStack wrong = new ItemStack(Items.DIRT, 4);
            check(!barrel.canPlaceItem(0, wrong), "Items that the task does not need are not accepted");
            ItemStack delivery = new ItemStack(Items.COBBLESTONE, 3);
            check(barrel.canPlaceItem(0, delivery), "Items that the task needs are accepted");
            barrel.setItem(0, delivery);
            play.expectRatio(0.6F, "After delivering 3 of 5");

            ItemStack rest = new ItemStack(Items.COBBLESTONE, 4);
            barrel.setItem(0, rest);
            check(play.task().isCompleted(play.id()), "Completed after delivering 7 of 5");
            equal(2, rest.getCount(), "The items that were not needed are left in the delivery");
            check(!barrel.canPlaceItem(0, new ItemStack(Items.COBBLESTONE)), "A completed task accepts no more items");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void breakingBlocksIsCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("block_break", part("minecraft:stone", 2), "blocks"));
            BlockPos first = new BlockPos(3, 1, 3), second = new BlockPos(4, 1, 3), other = new BlockPos(5, 1, 3);
            helper.setBlock(first, Blocks.STONE);
            helper.setBlock(second, Blocks.STONE);
            helper.setBlock(other, Blocks.DIRT);

            play.player().gameMode.destroyBlock(helper.absolutePos(other));
            play.expectRatio(0, "After breaking dirt");
            play.player().gameMode.destroyBlock(helper.absolutePos(first));
            play.expectRatio(0.5F, "After breaking one stone");
            play.player().gameMode.destroyBlock(helper.absolutePos(second));
            check(play.task().isCompleted(play.id()), "Completed after breaking two stones");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void placingBlocksIsCountedOnce(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, items("block_place", part("minecraft:bricks", 4), "items"));
            BlockPos ground = new BlockPos(3, 1, 3);
            helper.setBlock(ground, Blocks.STONE);
            BlockPos absolute = helper.absolutePos(ground);

            play.player().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BRICKS, 8));
            play.player().gameMode.useItemOn(play.player(), helper.getLevel(), play.player().getItemInHand(InteractionHand.MAIN_HAND), InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(absolute).add(0, 0.5, 0), Direction.UP, absolute, false));

            check(helper.getLevel().getBlockState(absolute.above()).is(Blocks.BRICKS), "A brick should have been placed");
            play.expectRatio(0.25F, "After placing one of four bricks");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void killingMobsIsCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, json("{\"type\": \"hardcorequesting:kill\", \"mobs\": [{\"mob\": \"minecraft:zombie\", \"kills\": 2}]}"));
            ServerPlayer player = play.player();

            Creeper creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new BlockPos(3, 1, 3));
            creeper.hurt(player.damageSources().playerAttack(player), 1000);
            play.expectRatio(0, "After killing a creeper");

            Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(3, 1, 4));
            zombie.hurt(player.damageSources().playerAttack(player), 1000);
            play.expectRatio(0.5F, "After killing one zombie");

            Zombie unrelated = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 3));
            unrelated.hurt(player.damageSources().generic(), 1000);
            play.expectRatio(0.5F, "A zombie that was not killed by the player");

            Zombie second = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 4));
            second.hurt(player.damageSources().playerAttack(player), 1000);
            check(play.task().isCompleted(play.id()), "Completed after killing two zombies");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void tamingAnimalsIsCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, json("{\"type\": \"hardcorequesting:tame\", \"part\": [{\"tame\": \"minecraft:wolf\", \"tames\": 2}]}"));

            Cat cat = helper.spawnWithNoFreeWill(EntityType.CAT, new BlockPos(3, 1, 3));
            cat.tame(play.player());
            play.expectRatio(0, "After taming a cat");

            Wolf first = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(3, 1, 4));
            first.tame(play.player());
            play.expectRatio(0.5F, "After taming one wolf");

            Wolf second = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(4, 1, 3));
            second.tame(play.player());
            check(play.task().isCompleted(play.id()), "Completed after taming two wolves");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void deathsAreCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, json("{\"type\": \"hardcorequesting:death\", \"deaths\": 2}"));

            EventTrigger.instance().onLivingDeath(play.player(), play.player().damageSources().generic());
            play.expectRatio(0.5F, "After one death");
            check(!play.task().isCompleted(play.id()), "Not completed after one of two deaths");
            EventTrigger.instance().onLivingDeath(play.player(), play.player().damageSources().generic());
            check(play.task().isCompleted(play.id()), "Completed after two deaths");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void visitingLocationsIsCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            BlockPos here = BlockPos.containing(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
            String dimension = "\"minecraft:overworld\"";
            String parts = location(here, 5, dimension) + ","
                           + location(here.offset(1000, 0, 1000), 5, dimension) + ","
                           + location(here, 5, "\"minecraft:the_nether\"");
            Play play = play(t, json("{\"type\": \"hardcorequesting:location\", \"locations\": [" + parts + "]}"));

            EventTrigger.instance().onPlayerTick(play.player());
            play.expectRatio(1 / 3F, "At the first location");

            play.player().setPos(Vec3.atBottomCenterOf(here.offset(1000, 0, 1000)));
            EventTrigger.instance().onPlayerTick(play.player());
            play.expectRatio(2 / 3F, "At the second location");
            check(!play.task().isCompleted(play.id()), "The location in the other dimension was not visited");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void visitingEveryLocationCompletesTheTask(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            BlockPos here = BlockPos.containing(helper.absoluteVec(new Vec3(1.5, 1, 1.5)));
            BlockPos far = here.offset(1000, 0, 1000);
            String dimension = "\"minecraft:overworld\"";
            Play play = play(t, json("{\"type\": \"hardcorequesting:location\", \"locations\": [" + location(here, 5, dimension) + "," + location(far, 5, dimension) + "]}"));
            
            EventTrigger.instance().onPlayerTick(play.player());
            check(!play.task().isCompleted(play.id()), "Not completed after the first of two locations");
            
            play.player().setPos(Vec3.atBottomCenterOf(far));
            EventTrigger.instance().onPlayerTick(play.player());
            check(play.task().isCompleted(play.id()), "Completed after both locations");
        });
    }
    
    private static String location(BlockPos pos, int radius, String dimension) {
        return "{\"x\": " + pos.getX() + ", \"y\": " + pos.getY() + ", \"z\": " + pos.getZ() + ", \"dim\": " + dimension
               + ", \"radius\": " + radius + ", \"visible\": \"FULL\"}";
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void reputationIsCheckedForTheTeam(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // Liked (50) or better
            Play play = play(t, json("{\"type\": \"hardcorequesting:reputation\", \"reputation\": [{\"reputation\": \"rep1\", \"inverted\": false, \"lower\": 2}]}"));

            play.team().setReputation("rep1", 10);
            EventTrigger.instance().onReputationChange(new EventTrigger.ReputationEvent(play.player()));
            check(!play.task().isCompleted(play.id()), "Not completed with a reputation of 10");

            play.team().setReputation("rep1", 60);
            EventTrigger.instance().onReputationChange(new EventTrigger.ReputationEvent(play.player()));
            check(play.task().isCompleted(play.id()), "Completed with a reputation of 60");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void killingPlayersWithTheRightReputationIsCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // Victims that are between Hated (-100) and Disliked (-50)
            Play play = play(t, json("{\"type\": \"hardcorequesting:reputation_kill\", \"kills\": 2, "
                                     + "\"reputation\": [{\"reputation\": \"rep1\", \"inverted\": false, \"lower\": 0, \"upper\": 1}]}"));
            ServerPlayer neutral = helper.makeMockServerPlayerInLevel();
            ServerPlayer hated = helper.makeMockServerPlayerInLevel();
            QuestingDataManager.getInstance().getQuestingData(hated).getTeam().setReputation("rep1", -60);

            EventTrigger.instance().onLivingDeath(neutral, play.player().damageSources().playerAttack(play.player()));
            play.expectRatio(0, "After killing a player with a neutral reputation");

            EventTrigger.instance().onLivingDeath(hated, play.player().damageSources().playerAttack(play.player()));
            play.expectRatio(0.5F, "After killing one hated player");
            EventTrigger.instance().onLivingDeath(hated, play.player().damageSources().playerAttack(play.player()));
            check(play.task().isCompleted(play.id()), "Completed after killing two hated players");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void advancementsAreCounted(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Play play = play(t, json("{\"type\": \"hardcorequesting:advancement\", \"advancements\": [{\"adv_name\": \"minecraft:story/mine_stone\"}]}"));
            MinecraftServer server = helper.getLevel().getServer();
            AdvancementHolder advancement = server.getAdvancements().get(ResourceLocation.parse("minecraft:story/mine_stone"));
            check(advancement != null, "The advancement does not exist");
            PlayerAdvancements advancements = server.getPlayerList().getPlayerAdvancements(play.player());

            check(!play.task().isCompleted(play.id()), "Not completed without the advancement");
            for (String criterion : advancement.value().criteria().keySet())
                advancements.award(advancement, criterion);

            check(play.task().isCompleted(play.id()), "Completed when the advancement is awarded");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void completingQuestsCompletesTheTask(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject task = json("{\"type\": \"hardcorequesting:completion\", \"completed_quests\": [{\"quest_uuid\": \"" + id("other") + "\"}]}");
            Play play = play(t, task, checkboxQuest("other"));
            Quest other = play.line().questSetsManager.quests.get(id("other"));

            EventTrigger.instance().onQuestComplete(new EventTrigger.QuestCompletedEvent(play.player(), other.getQuestId()));
            check(!play.task().isCompleted(play.id()), "Not completed while the other quest is open");

            other.getQuestData(play.id()).completed = true;
            EventTrigger.instance().onQuestComplete(new EventTrigger.QuestCompletedEvent(play.player(), other.getQuestId()));
            check(play.task().isCompleted(play.id()), "Completed when the other quest is completed");
        });
    }
}
