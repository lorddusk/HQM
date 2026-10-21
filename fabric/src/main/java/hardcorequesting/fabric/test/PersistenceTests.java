package hardcorequesting.fabric.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.bag.LootGroup;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.death.DeathType;
import hardcorequesting.common.io.FileDataManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.team.LifeSetting;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;

/**
 * Saving and loading of books and world data.
 * <p>
 * Tests marked {@code required = false} describe the wanted behaviour for bad input, which the mod does not have yet.
 * They are reported, but do not fail the build.
 */
public class PersistenceTests {

    private static JsonElement file(MemoryData data, String name) {
        return parse(data.read(name).orElseThrow(() -> new AssertionError("The file " + name + " was not written, only " + data.files().keySet())));
    }

    /**
     * Compares two files as json if they are json, and as text otherwise.
     */
    private static void sameFile(String name, String expected, String actual) {
        try {
            equal(parse(expected), parse(actual), "Content of " + name);
        } catch (RuntimeException notJson) {
            equal(expected, actual, "Content of " + name);
        }
    }
    
    /**
     * The same json with all arrays sorted, for data that is written in an order that is not defined (e.g. from a hash map).
     */
    private static JsonElement unordered(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            element.getAsJsonObject().entrySet().forEach(entry -> result.add(entry.getKey(), unordered(entry.getValue())));
            return result;
        } else if (element.isJsonArray()) {
            List<JsonElement> items = new ArrayList<>();
            element.getAsJsonArray().forEach(item -> items.add(unordered(item)));
            items.sort(Comparator.comparing(JsonElement::toString));
            JsonArray result = new JsonArray();
            items.forEach(result::add);
            return result;
        }
        return element;
    }
    
    private static void sameWorldFile(String name, String expected, String actual) {
        equal(unordered(parse(expected)), unordered(parse(actual)), "Content of " + name);
    }
    
    private static JsonObject questById(JsonElement set, UUID id) {
        for (JsonElement quest : set.getAsJsonObject().getAsJsonArray("quests"))
            if (quest.getAsJsonObject().get("uuid").getAsString().equals(id.toString()))
                return quest.getAsJsonObject();
        throw new AssertionError("The quest " + id + " is missing in the saved set");
    }

    // Books

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void bookLoadsAllContent(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(fullBook());

            equal(2, line.questSetsManager.questSets.size(), "Number of quest sets");
            equal(SET_ALPHA, line.questSetsManager.questSets.get(0).getName(), "First set");
            equal(SET_BETA, line.questSetsManager.questSets.get(1).getName(), "Second set");
            equal(taskSamples().size() + 10, line.questSetsManager.quests.size(), "Number of quests");
            equal(2, line.reputationManager.size(), "Number of reputations");
            equal(2, line.groupTierManager.getTiers().size(), "Number of loot tiers");
            equal(2, LootGroup.getGroups().size(), "Number of loot groups");
            equal("A book for tests", line.getMainDescription(), "Book description");

            Quest quest = line.questSetsManager.quests.get(id("task_consume"));
            check(quest != null, "The consume quest was not loaded");
            equal(1, quest.getTasks().size(), "Tasks of the consume quest");
            check(line.questSetsManager.quests.get(id("linked")).getRequirements().size() == 2, "The linked quest should have two prerequisites");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void everyTaskTypeHasASample(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Set<String> registered = TaskType.values().stream().map(type -> type.toDataName().replace("hardcorequesting:", "")).collect(Collectors.toSet());
            for (String type : registered)
                check(taskSamples().containsKey(type), "The task type " + type + " has no sample in BookFixtures.taskSamples. Add one so it gets tested.");
            for (String type : taskSamples().keySet())
                check(registered.contains(type), "The sample " + type + " is not a registered task type");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void everyTaskTypeSurvivesSaveAndLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            MemoryData saved = TestScope.saveBook(t.loadBook(fullBook()));
            JsonElement alpha = file(saved, "sets/" + SET_ALPHA + ".json");

            for (Map.Entry<String, JsonObject> sample : taskSamples().entrySet()) {
                JsonObject quest = questById(alpha, id("task_" + sample.getKey()));
                JsonArray tasks = quest.getAsJsonArray("tasks");
                check(tasks != null && tasks.size() == 1, "The " + sample.getKey() + " task was not saved");
                TestScope.contains(sample.getValue(), tasks.get(0), sample.getKey());
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questSettingsSurviveSaveAndLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            MemoryData saved = TestScope.saveBook(t.loadBook(fullBook()));
            JsonElement beta = file(saved, "sets/" + SET_BETA.replace(" ", "_") + ".json");

            JsonObject expected = betaSet();
            for (JsonElement quest : expected.getAsJsonArray("quests")) {
                UUID id = UUID.fromString(quest.getAsJsonObject().get("uuid").getAsString());
                TestScope.contains(quest, questById(beta, id), quest.getAsJsonObject().get("name").getAsJsonObject().get("text").getAsString());
            }
            TestScope.contains(expected.get("reputationBar"), beta.getAsJsonObject().get("reputationBar"), "reputationBar");

            JsonElement alpha = file(saved, "sets/" + SET_ALPHA + ".json");
            TestScope.contains(alphaSet().get("description"), alpha.getAsJsonObject().get("description"), "set description");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void reputationsSurviveSaveAndLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonArray saved = file(TestScope.saveBook(t.loadBook(fullBook())), "reputations.json").getAsJsonArray();
            equal(2, saved.size(), "Number of saved reputations");
            for (JsonElement expected : reputations()) {
                String id = expected.getAsJsonObject().get("id").getAsString();
                JsonElement actual = null;
                for (JsonElement candidate : saved)
                    if (candidate.getAsJsonObject().get("id").getAsString().equals(id))
                        actual = candidate;
                check(actual != null, "The reputation " + id + " was not saved");
                TestScope.contains(expected, actual, id);
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void lootTiersSurviveSaveAndLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonElement saved = file(TestScope.saveBook(t.loadBook(fullBook())), "bags.json");
            TestScope.contains(lootTiers(), saved, "bags");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void savingTwiceGivesTheSameFiles(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            MemoryData first = TestScope.saveBook(t.loadBook(fullBook()));
            MemoryData second = TestScope.saveBook(t.loadBook(first));

            equal(first.files().keySet(), second.files().keySet(), "Saved files");
            for (String name : first.files().keySet())
                sameFile(name, first.files().get(name), second.files().get(name));
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void setOrderSurvivesSaveAndLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            MemoryData book = bookWith(betaSet(), alphaSet()).with("reputations.json", reputations().toString());
            QuestLine line = t.loadBook(book);
            equal(SET_BETA, line.questSetsManager.questSets.get(0).getName(), "First set");

            MemoryData saved = TestScope.saveBook(line);
            equal(parse("{\"sets\": [\"Beta_Set\", \"Alpha\"]}"), file(saved, "sets.json"), "Set list");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void bookSurvivesFileStorage(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            Path directory = Files.createTempDirectory("hqm-test-book");
            try {
                QuestLine original = t.loadBook(fullBook());
                MemoryData inMemory = TestScope.saveBook(original);
                original.saveQuests(new FileDataManager(directory));
                for (String name : inMemory.files().keySet())
                    check(Files.exists(directory.resolve(name)), "The file " + name + " was not written to the disk");
                
                QuestLine fromDisk = t.newLine();
                fromDisk.loadAll(new FileDataManager(directory), new MemoryData());
                MemoryData savedAgain = TestScope.saveBook(fromDisk);
                equal(inMemory.files().keySet(), savedAgain.files().keySet(), "Saved files");
                for (String name : inMemory.files().keySet())
                    sameFile(name, inMemory.files().get(name), savedAgain.files().get(name));
            } finally {
                deleteQuietly(directory);
            }
        });
    }
    
    private static void deleteQuietly(Path directory) {
        try {
            FileUtils.deleteDirectory(directory.toFile());
        } catch (IOException ignored) {
        }
    }

    // World data

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void worldDataSurvivesSaveAndLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(fullBook());
            UUID alice = id("player_alice"), bob = id("player_bob"), carol = id("player_carol");

            QuestingDataManager manager = QuestingDataManager.getInstance();
            manager.activateQuest(false);
            QuestingData aliceData = manager.getQuestingData(alice);
            aliceData.setName("Alice");
            aliceData.setRawLives(7);
            aliceData.selectedQuestId = id("rewards");
            aliceData.selectedTask = 0;
            aliceData.playedLore = true;
            aliceData.receivedBook = true;
            aliceData.getGroupData(id("group_Plain")).retrieved = 2;
            QuestingData bobData = manager.getQuestingData(bob);
            bobData.setName("Bob");
            bobData.setRawLives(3);
            QuestingData carolData = manager.getQuestingData(carol);
            carolData.setName("Carol");
            carolData.setRawLives(5);

            Team squad = Team.single(alice);
            squad.setId(id("team_squad"));
            TeamManager.getInstance().addTeam(squad);
            squad.setName("Squad");
            squad.addPlayer(new PlayerEntry(bob, true, false));
            squad.setRewardSetting(RewardSetting.ANY);
            squad.setLifeSetting(LifeSetting.INDIVIDUAL);
            squad.setReputation("rep1", 42);
            aliceData.setTeam(squad);
            bobData.setTeam(squad);

            Quest quest = Quest.getQuest(id("task_checkbox"));
            quest.getQuestData(carol).completed = true;
            quest.getQuestData(carol).available = false;
            quest.getQuestData(carol).time = 12;
            quest.getTasks().get(0).getData(carol).completed = true;

            DeathStatsManager.getInstance().getDeathStat(alice).increaseDeath(DeathType.FALL, 3, false);
            DeathStatsManager.getInstance().getDeathStat(alice).increaseDeath(DeathType.LAVA, 1, false);

            MemoryData world = TestScope.saveWorld(line);
            for (String name : new String[]{"state.json", "data.json", "teams.json", "deaths.json"})
                check(world.files().containsKey(name), "The world file " + name + " was not written, only " + world.files().keySet());

            QuestLine loaded = t.load(fullBook(), world);
            QuestingDataManager loadedManager = loaded.questingDataManager;
            check(loadedManager.isQuestActive(), "Questing mode was not restored");

            QuestingData alice2 = loadedManager.getQuestingData(alice);
            equal(7, alice2.getRawLives(), "Lives of Alice");
            equal("Alice", alice2.getName(), "Name of Alice");
            equal(id("rewards"), alice2.selectedQuestId, "Selected quest of Alice");
            check(alice2.playedLore && alice2.receivedBook, "The lore and book flags of Alice were not restored");
            equal(2, alice2.getGroupData(id("group_Plain")).retrieved, "Retrieved loot of Alice");
            equal(3, loadedManager.getQuestingData(bob).getRawLives(), "Lives of Bob");

            Team squad2 = alice2.getTeam();
            check(!squad2.isSingle(), "Alice is not in the team after loading");
            equal("Squad", squad2.getName(), "Team name");
            equal(2, squad2.getPlayerCount(), "Team size");
            check(loadedManager.getQuestingData(bob).getTeam() == squad2, "Bob is not in the same team");
            equal(RewardSetting.ANY, squad2.getRewardSetting(), "Reward setting");
            equal(LifeSetting.INDIVIDUAL, squad2.getLifeSetting(), "Life setting");
            equal(42, squad2.getReputation("rep1"), "Team reputation");
            check(loadedManager.getQuestingData(carol).getTeam().isSingle(), "Carol should be alone");

            Quest quest2 = Quest.getQuest(id("task_checkbox"));
            check(quest2.getQuestData(carol).completed && !quest2.getQuestData(carol).available, "The quest progress of Carol was not restored");
            equal(12L, quest2.getQuestData(carol).time, "Quest time of Carol");
            check(quest2.getTasks().get(0).getData(carol).completed, "The task progress of Carol was not restored");
            check(!quest2.getQuestData(alice).completed, "The squad did not complete the quest");

            equal(3, DeathStatsManager.getInstance().getDeathStat(alice).getDeaths(DeathType.FALL), "Fall deaths of Alice");
            equal(1, DeathStatsManager.getInstance().getDeathStat(alice).getDeaths(DeathType.LAVA), "Lava deaths of Alice");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void savingWorldDataTwiceGivesTheSameFiles(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(fullBook());
            QuestingDataManager manager = QuestingDataManager.getInstance();
            manager.activateQuest(false);
            QuestingData data = manager.getQuestingData(id("player_alice"));
            data.setName("Alice");
            data.setRawLives(4);
            Team squad = Team.single(id("player_alice"));
            squad.setId(id("team_squad"));
            TeamManager.getInstance().addTeam(squad);
            squad.setName("Squad");
            data.setTeam(squad);
            DeathStatsManager.getInstance().getDeathStat(id("player_alice")).increaseDeath(DeathType.FALL, 2, false);

            MemoryData first = TestScope.saveWorld(line);
            MemoryData second = TestScope.saveWorld(t.load(fullBook(), first));

            equal(first.files().keySet(), second.files().keySet(), "Saved world files");
            for (String name : first.files().keySet())
                sameWorldFile(name, first.files().get(name), second.files().get(name));
        });
    }

    // Bad input

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void missingFilesGiveAnEmptyBook(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(new MemoryData());
            equal(0, line.questSetsManager.questSets.size(), "Number of quest sets");
            equal(0, line.questSetsManager.quests.size(), "Number of quests");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void setsInTheOldListFormatLoad(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // Before sets.json became an object, it was a list of names, and the set files were next to it
            MemoryData book = new MemoryData()
                    .with("sets.json", "[\"Beta Set\", \"Alpha\"]")
                    .with("Alpha.json", alphaSet().toString())
                    .with("Beta_Set.json", betaSet().toString())
                    .with("reputations.json", reputations().toString());
            QuestLine line = t.loadBook(book);

            equal(2, line.questSetsManager.questSets.size(), "Number of quest sets");
            equal(taskSamples().size() + 10, line.questSetsManager.quests.size(), "Number of quests");
            equal(SET_BETA, line.questSetsManager.questSets.get(0).getName(), "The order of the list is kept");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questWithoutIdIsDropped(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject noId = quest("no_id", 0, 0);
            noId.remove("uuid");
            QuestLine line = t.loadBook(bookWith(setOf("Set", noId, quest("fine", 45, 0))));

            equal(1, line.questSetsManager.quests.size(), "Number of quests");
            check(line.questSetsManager.quests.containsKey(id("fine")), "The quest with an id was not loaded");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void linksToMissingQuestsAreIgnored(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject linked = quest("linked", 0, 0);
            JsonArray missing = new JsonArray();
            missing.add(id("does_not_exist").toString());
            linked.add("prerequisites", missing);
            linked.add("optionlinks", missing.deepCopy());
            QuestLine line = t.loadBook(bookWith(setOf("Set", linked)));

            Quest quest = line.questSetsManager.quests.get(id("linked"));
            check(quest != null, "The quest was not loaded");
            equal(0, quest.getRequirements().size(), "Prerequisites");
            equal(0, quest.getOptionLinks().size(), "Option links");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void unknownItemsInRewardsAreSkipped(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject quest = quest("rewards", 0, 0);
            JsonArray rewards = new JsonArray();
            rewards.add(stack("minecraft:this_item_does_not_exist"));
            rewards.add(stack("minecraft:diamond", 2));
            quest.add("reward", rewards);
            QuestLine line = t.loadBook(bookWith(setOf("Set", quest)));

            equal(1, line.questSetsManager.quests.get(id("rewards")).getRewards().getReward().size(), "Number of reward items");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void malformedSetFileIsSkipped(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            MemoryData book = bookWith(alphaSet(), betaSet());
            book.write("sets/Alpha.json", "{ this is not json");
            QuestLine line = t.loadBook(book);

            equal(1, line.questSetsManager.questSets.size(), "The other set should still load");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void corruptSetListKeepsTheRestOfTheBook(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            for (String corrupt : new String[]{"{ this is not json", "{\"sets\": 5}", "{}", "\"text\""}) {
                MemoryData book = bookWith(alphaSet(), betaSet()).with("sets.json", corrupt).with("description.txt", "Kept description");
                QuestLine line = t.loadBook(book);

                equal(0, line.questSetsManager.questSets.size(), "Number of quest sets with the set list " + corrupt);
                equal(0, line.questSetsManager.quests.size(), "Number of quests with the set list " + corrupt);
                equal("Kept description", line.getMainDescription(), "The description is not affected by the set list " + corrupt);
                String kept = book.files().entrySet().stream().filter(entry -> entry.getKey().startsWith("sets.json.corrupt")).map(Map.Entry::getValue).findFirst().orElse(null);
                equal(corrupt, kept, "Content kept of the set list");
                check(book.files().containsKey("sets/Alpha.json"), "The set files should be left alone");
                
                MemoryData saved = TestScope.saveBook(line);   // and the book can be saved and loaded again
                t.loadBook(saved);
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questWithUnknownTaskTypeIsDroppedAlone(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // For example a task of a mod that was removed. Progress is kept by the position of the tasks, so the quest is dropped as a whole.
            JsonObject unknown = questWithTask("unknown", 0, 0, json("{\"type\": \"somemod:nothing\"}"));
            JsonObject child = requiring(checkboxQuest("child"), "unknown");
            QuestLine line = t.loadBook(bookWith(setOf("Set", unknown, quest("fine", 45, 0), child), setOf("Other", checkboxQuest("other"))));
            
            check(line.questSetsManager.quests.containsKey(id("fine")), "The other quest of the set should still load");
            check(line.questSetsManager.quests.containsKey(id("child")), "The quest that waited for the dropped quest should still load");
            check(line.questSetsManager.quests.containsKey(id("other")), "The other set should still load");
            check(!line.questSetsManager.quests.containsKey(id("unknown")), "The quest with the unknown task should be dropped");
            equal(2, line.questSetsManager.questSets.size(), "Number of quest sets");
            equal(2, line.questSetsManager.questSets.get(0).getQuests().size(), "Quests in the set of the dropped quest");
            equal(0, line.questSetsManager.quests.get(id("child")).getRequirements().size(), "The link to the dropped quest is gone");
        });
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void sameQuestIdInTwoSetsIsNotMerged(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // For example a set that was exported, renamed and imported again
            QuestLine line = t.loadBook(bookWith(setOf("First", quest("same", 0, 0)), setOf("Second", quest("same", 0, 0))));

            equal(2, line.questSetsManager.quests.size(), "Both quests should be kept, with different ids");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void corruptWorldFileKeepsTheRest(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            String corrupt = "{ this is not json";
            MemoryData world = new MemoryData().with("data.json", corrupt).with("teams.json", "[1, 2").with("state.json", "{\"questing\": true, \"hardcore\": false}");
            QuestLine line = t.load(fullBook(), world);
            
            check(line.questingDataManager.isQuestActive(), "The readable state file should still be applied");
            equal(taskSamples().size() + 10, line.questSetsManager.quests.size(), "The book should be loaded");
            
            // What could not be read is kept next to the file, so it is not lost when the world is saved over it
            for (String name : new String[]{"data.json", "teams.json"}) {
                String kept = world.files().entrySet().stream().filter(entry -> entry.getKey().startsWith(name + ".corrupt")).map(Map.Entry::getValue).findFirst().orElse(null);
                equal(world.files().get(name), kept, "Content kept of " + name);
            }
            
            // And the world can be saved and loaded again
            MemoryData saved = TestScope.saveWorld(line);
            check(saved.files().containsKey("data.json"), "The world data should be saved");
            t.load(fullBook(), saved);
        });
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void reputationRewardOfMissingReputationDoesNotBreakSaving(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            // The book has no reputations.json, so the reputation of the reward does not exist
            JsonObject quest = quest("reward", 0, 0);
            JsonArray reputationRewards = new JsonArray();
            JsonObject reward = new JsonObject();
            reward.addProperty("reputation", "missing");
            reward.addProperty("value", 10);
            reputationRewards.add(reward);
            quest.add("reputationrewards", reputationRewards);
            quest.add("name", text("Kept"));
            QuestLine line = t.loadBook(bookWith(setOf("Set", quest, quest("fine", 45, 0))));
            
            MemoryData saved = TestScope.saveBook(line);
            check(saved.files().containsKey("sets/Set.json"), "The set could not be saved");
        });
    }
}
