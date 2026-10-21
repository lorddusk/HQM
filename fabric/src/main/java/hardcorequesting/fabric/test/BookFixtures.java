package hardcorequesting.fabric.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Book files for the tests. They are written the way the mod saves them, so they can be compared to what it saves.
 */
public final class BookFixtures {
    private BookFixtures() {
    }

    public static final String SET_ALPHA = "Alpha";
    public static final String SET_BETA = "Beta Set";

    public static UUID id(String key) {
        return UUID.nameUUIDFromBytes(("hqm-test:" + key).getBytes(StandardCharsets.UTF_8));
    }

    public static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    public static JsonObject text(String text) {
        JsonObject object = new JsonObject();
        object.addProperty("text", text);
        object.addProperty("isTranslationKey", false);
        return object;
    }

    public static JsonObject stack(String item) {
        return stack(item, 1);
    }

    public static JsonObject stack(String item, int count) {
        JsonObject object = new JsonObject();
        object.addProperty("id", item);
        object.addProperty("Count", count);
        return object;
    }

    /**
     * A task of every registered type, with content, by the path of the type (e.g. {@code consume_qds}).
     */
    public static Map<String, JsonObject> taskSamples() {
        Map<String, JsonObject> tasks = new LinkedHashMap<>();
        tasks.put("checkbox", json("""
                {"type": "hardcorequesting:checkbox"}"""));
        tasks.put("consume", json("""
                {"type": "hardcorequesting:consume", "items": [
                  {"item": {"id": "minecraft:bread", "Count": 1}, "required": 5},
                  {"item": {"id": "minecraft:apple", "Count": 1}, "required": 3, "precision": "FUZZY"},
                  {"fluid": {"fluid": "minecraft:water", "volume": {"n": 1000, "d": 1}}}]}"""));
        tasks.put("craft", json("""
                {"type": "hardcorequesting:craft", "items": [
                  {"item": {"id": "minecraft:stick", "Count": 1}, "required": 8},
                  {"item": {"id": "minecraft:torch", "Count": 1}, "required": 2}]}"""));
        tasks.put("location", json("""
                {"type": "hardcorequesting:location", "locations": [
                  {"name": {"text": "Spawn", "isTranslationKey": false}, "x": 0, "y": 64, "z": -5, "dim": "minecraft:overworld",
                   "radius": 10, "visible": "FULL", "icon": {"id": "minecraft:grass_block", "Count": 1}},
                  {"name": {"text": "Nether", "isTranslationKey": false}, "x": 100, "y": 70, "z": -50, "dim": "minecraft:the_nether",
                   "radius": 5, "visible": "LOCATION", "icon": {"id": "minecraft:netherrack", "Count": 1}},
                  {"name": {"text": "End", "isTranslationKey": false}, "x": 0, "y": 64, "z": 0, "dim": "minecraft:the_end",
                   "radius": 20, "visible": "NONE", "icon": {"id": "minecraft:end_stone", "Count": 1}}]}"""));
        tasks.put("consume_qds", json("""
                {"type": "hardcorequesting:consume_qds", "items": [
                  {"item": {"id": "minecraft:cobblestone", "Count": 1}, "required": 256},
                  {"item": {"id": "minecraft:dirt", "Count": 1}, "required": 64, "precision": "FUZZY"}]}"""));
        tasks.put("detect", json("""
                {"type": "hardcorequesting:detect", "items": [
                  {"item": {"id": "minecraft:iron_ingot", "Count": 1}, "required": 16},
                  {"item": {"id": "minecraft:diamond", "Count": 1}}]}"""));
        tasks.put("kill", json("""
                {"type": "hardcorequesting:kill", "mobs": [
                  {"name": {"text": "Zombies", "isTranslationKey": false}, "icon": {"id": "minecraft:zombie_head", "Count": 1},
                   "mob": "minecraft:zombie", "kills": 10},
                  {"name": {"text": "Creepers", "isTranslationKey": false}, "icon": {"id": "minecraft:creeper_head", "Count": 1},
                   "mob": "minecraft:creeper", "kills": 3}]}"""));
        tasks.put("tame", json("""
                {"type": "hardcorequesting:tame", "part": [
                  {"name": {"text": "Wolf", "isTranslationKey": false}, "icon": {"id": "minecraft:wolf_spawn_egg", "Count": 1},
                   "tame": "minecraft:wolf", "tames": 2},
                  {"name": {"text": "Any horse", "isTranslationKey": false}, "icon": {"id": "minecraft:saddle", "Count": 1},
                   "tame": "abstracthorse", "tames": 1}]}"""));
        tasks.put("death", json("""
                {"type": "hardcorequesting:death", "deaths": 3}"""));
        tasks.put("reputation", json("""
                {"type": "hardcorequesting:reputation", "reputation": [
                  {"reputation": "rep1", "inverted": false, "lower": 0, "upper": 2},
                  {"reputation": "rep2", "inverted": true, "lower": 1}]}"""));
        tasks.put("reputation_kill", json("""
                {"type": "hardcorequesting:reputation_kill", "reputation": [
                  {"reputation": "rep1", "inverted": false, "lower": 0, "upper": 1}], "kills": 5}"""));
        tasks.put("advancement", json("""
                {"type": "hardcorequesting:advancement", "advancements": [
                  {"name": {"text": "Stone Age", "isTranslationKey": false}, "icon": {"id": "minecraft:cobblestone", "Count": 1},
                   "adv_name": "minecraft:story/mine_stone"},
                  {"name": {"text": "Diamonds", "isTranslationKey": false}, "icon": {"id": "minecraft:diamond", "Count": 1},
                   "adv_name": "minecraft:story/mine_diamond"}]}"""));
        JsonObject completion = json("""
                {"type": "hardcorequesting:completion", "completed_quests": []}""");
        completion.getAsJsonArray("completed_quests").add(json("{}"));
        completion.getAsJsonArray("completed_quests").get(0).getAsJsonObject().addProperty("quest_uuid", id("task_checkbox").toString());
        tasks.put("completion", completion);
        tasks.put("block_break", json("""
                {"type": "hardcorequesting:block_break", "blocks": [
                  {"item": {"id": "minecraft:stone", "Count": 1}, "required": 10},
                  {"item": {"id": "minecraft:oak_log", "Count": 1}, "required": 4}]}"""));
        tasks.put("block_place", json("""
                {"type": "hardcorequesting:block_place", "items": [
                  {"item": {"id": "minecraft:bricks", "Count": 1}, "required": 20},
                  {"item": {"id": "minecraft:glass", "Count": 1}, "required": 4}]}"""));
        return tasks;
    }

    public static JsonObject quest(String key, int x, int y) {
        JsonObject quest = new JsonObject();
        quest.addProperty("uuid", id(key).toString());
        quest.add("name", text("Quest " + key));
        quest.add("description", text("Description of " + key));
        quest.addProperty("x", x);
        quest.addProperty("y", y);
        quest.add("icon", stack("minecraft:book"));
        quest.add("prerequisites", new JsonArray());
        quest.add("optionlinks", new JsonArray());
        quest.add("reward", new JsonArray());
        quest.add("rewardchoice", new JsonArray());
        quest.add("commandrewards", new JsonArray());
        return quest;
    }

    public static JsonObject questWithTask(String key, int x, int y, JsonObject task) {
        JsonObject quest = quest(key, x, y);
        JsonArray tasks = new JsonArray();
        tasks.add(task);
        quest.add("tasks", tasks);
        return quest;
    }

    /**
     * The quests of the first set, one for each task type.
     */
    public static JsonObject alphaSet() {
        JsonArray quests = new JsonArray();
        int i = 0;
        for (Map.Entry<String, JsonObject> task : taskSamples().entrySet()) {
            quests.add(questWithTask("task_" + task.getKey(), 20 + (i % 5) * 45, 20 + (i / 5) * 45, task.getValue()));
            i++;
        }
        JsonObject set = new JsonObject();
        set.addProperty("name", SET_ALPHA);
        set.add("quests", quests);
        set.add("reputationBar", new JsonArray());
        set.add("description", text("The first set"));
        return set;
    }

    /**
     * Quests with all kinds of quest settings.
     */
    public static JsonObject betaSet() {
        JsonArray quests = new JsonArray();

        JsonObject rewards = questWithTask("rewards", 20, 20, taskSamples().get("checkbox"));
        JsonArray reward = new JsonArray();
        reward.add(stack("minecraft:diamond", 3));
        JsonObject named = stack("minecraft:diamond_sword");
        JsonObject components = new JsonObject();
        components.addProperty("minecraft:custom_name", "{\"color\":\"gold\",\"text\":\"Reward Blade\"}");
        named.add("components", components);
        reward.add(named);
        rewards.add("reward", reward);
        JsonArray choice = new JsonArray();
        choice.add(stack("minecraft:iron_pickaxe"));
        choice.add(stack("minecraft:iron_axe"));
        rewards.add("rewardchoice", choice);
        JsonArray reputationRewards = new JsonArray();
        JsonObject reputationReward = new JsonObject();
        reputationReward.addProperty("reputation", "rep1");
        reputationReward.addProperty("value", 50);
        reputationRewards.add(reputationReward);
        rewards.add("reputationrewards", reputationRewards);
        JsonArray commands = new JsonArray();
        commands.add("say Quest completed");
        rewards.add("commandrewards", commands);
        quests.add(rewards);

        JsonObject big = questWithTask("big_icon", 65, 20, taskSamples().get("checkbox"));
        big.addProperty("bigicon", true);
        quests.add(big);

        JsonObject fluidIcon = questWithTask("fluid_icon", 110, 20, taskSamples().get("checkbox"));
        fluidIcon.remove("icon");
        fluidIcon.add("fluid_icon", json("{\"fluid\": \"minecraft:lava\", \"volume\": {\"n\": 1000, \"d\": 1}}"));
        quests.add(fluidIcon);

        for (String[] repeat : new String[][]{{"instant", "INSTANT"}, {"interval", "INTERVAL"}, {"time", "TIME"}}) {
            JsonObject quest = questWithTask("repeat_" + repeat[0], 20 + quests.size() * 5, 65, taskSamples().get("checkbox"));
            JsonObject info = new JsonObject();
            info.addProperty("type", repeat[1]);
            if (!repeat[1].equals("INSTANT")) {
                info.addProperty("days", 1);
                info.addProperty("hours", 12);
            }
            quest.add("repeat", info);
            quests.add(quest);
        }

        for (String[] trigger : new String[][]{{"quest", "QUEST_TRIGGER"}, {"anti", "ANTI_TRIGGER"}}) {
            JsonObject quest = questWithTask("trigger_" + trigger[0], 20 + quests.size() * 5, 110, taskSamples().get("checkbox"));
            quest.addProperty("trigger", trigger[1]);
            quests.add(quest);
        }
        JsonObject taskTrigger = quest("trigger_task", 200, 110);
        JsonArray tasks = new JsonArray();
        for (int i = 0; i < 3; i++)
            tasks.add(taskSamples().get("checkbox"));
        taskTrigger.add("tasks", tasks);
        taskTrigger.addProperty("trigger", "TASK_TRIGGER");
        taskTrigger.addProperty("triggertasks", 2);
        quests.add(taskTrigger);

        JsonObject linked = questWithTask("linked", 20, 155, taskSamples().get("checkbox"));
        JsonArray prerequisites = new JsonArray();
        prerequisites.add(id("rewards").toString());
        prerequisites.add(id("big_icon").toString());
        linked.add("prerequisites", prerequisites);
        JsonArray optionLinks = new JsonArray();
        optionLinks.add(id("fluid_icon").toString());
        linked.add("optionlinks", optionLinks);
        linked.addProperty("parentrequirement", 1);
        quests.add(linked);

        JsonObject set = new JsonObject();
        set.addProperty("name", SET_BETA);
        set.add("quests", quests);
        JsonArray bars = new JsonArray();
        JsonObject bar = new JsonObject();
        bar.addProperty("reputationId", "rep1");
        bar.addProperty("x", 10);
        bar.addProperty("y", 200);
        bars.add(bar);
        set.add("reputationBar", bars);
        return set;
    }

    public static JsonArray reputations() {
        return JsonParser.parseString("""
                [{"id": "rep1", "name": {"text": "First reputation", "isTranslationKey": false},
                  "neutral": {"text": "Neutral", "isTranslationKey": false},
                  "markers": [{"name": {"text": "Hated", "isTranslationKey": false}, "value": -100},
                             {"name": {"text": "Disliked", "isTranslationKey": false}, "value": -50},
                             {"name": {"text": "Liked", "isTranslationKey": false}, "value": 50}]},
                 {"id": "rep2", "name": {"text": "Second reputation", "isTranslationKey": false},
                  "neutral": {"text": "Meh", "isTranslationKey": false},
                  "markers": [{"name": {"text": "Low", "isTranslationKey": false}, "value": -10},
                             {"name": {"text": "High", "isTranslationKey": false}, "value": 10}]}]""").getAsJsonArray();
    }

    public static JsonArray lootTiers() {
        JsonArray tiers = new JsonArray();
        for (String[] tier : new String[][]{{"Plain", "GRAY"}, {"Rare", "ORANGE"}}) {
            JsonObject object = new JsonObject();
            object.add("name", text(tier[0]));
            object.addProperty("colour", tier[1]);
            JsonArray weights = new JsonArray();
            for (int weight : new int[]{50, 40, 30, 20, 10})
                weights.add(weight);
            object.add("weights", weights);
            JsonArray groups = new JsonArray();
            JsonObject group = new JsonObject();
            group.addProperty("id", id("group_" + tier[0]).toString());
            group.add("name", text(tier[0] + " group"));
            group.addProperty("limit", 2);
            JsonArray items = new JsonArray();
            items.add(stack("minecraft:bread", 2));
            items.add(stack("minecraft:iron_ingot", 5));
            group.add("items", items);
            groups.add(group);
            object.add("groups", groups);
            tiers.add(object);
        }
        return tiers;
    }

    /**
     * A complete book: two sets with every task type and quest setting, reputations and loot tiers.
     */
    public static MemoryData fullBook() {
        JsonArray sets = new JsonArray();
        sets.add(SET_ALPHA);
        sets.add(SET_BETA.replace(" ", "_"));
        JsonObject list = new JsonObject();
        list.add("sets", sets);

        return new MemoryData()
                .with("sets.json", list.toString())
                .with("sets/" + SET_ALPHA + ".json", alphaSet().toString())
                .with("sets/" + SET_BETA.replace(" ", "_") + ".json", betaSet().toString())
                .with("reputations.json", reputations().toString())
                .with("bags.json", lootTiers().toString())
                .with("description.txt", "A book for tests");
    }

    /**
     * A book with the given sets. The set files are named after the sets.
     */
    public static MemoryData bookWith(JsonObject... sets) {
        JsonArray names = new JsonArray();
        MemoryData book = new MemoryData();
        for (JsonObject set : sets) {
            String name = set.get("name").getAsString().replace(" ", "_");
            names.add(name);
            book.write("sets/" + name + ".json", set.toString());
        }
        JsonObject list = new JsonObject();
        list.add("sets", names);
        return book.with("sets.json", list.toString());
    }
    
    public static JsonObject setOf(String name, JsonObject... quests) {
        JsonObject set = new JsonObject();
        set.addProperty("name", name);
        JsonArray array = new JsonArray();
        for (JsonObject quest : quests)
            array.add(quest);
        set.add("quests", array);
        set.add("reputationBar", new JsonArray());
        return set;
    }
    
    /**
     * A book with one set with the given quests, and the reputations of {@link #reputations()}.
     */
    public static MemoryData bookOf(JsonObject... quests) {
        return bookWith(setOf("Set", quests)).with("reputations.json", reputations().toString());
    }
    
    /**
     * Makes the quest require the other quests, by the keys they were created with.
     */
    public static JsonObject requiring(JsonObject quest, String... keys) {
        JsonArray array = new JsonArray();
        for (String key : keys)
            array.add(id(key).toString());
        quest.add("prerequisites", array);
        return quest;
    }
    
    /**
     * Links the quest to the other quests, by the keys they were created with.
     */
    public static JsonObject linking(JsonObject quest, String... keys) {
        JsonArray array = new JsonArray();
        for (String key : keys)
            array.add(id(key).toString());
        quest.add("optionlinks", array);
        return quest;
    }
    
    public static JsonObject checkboxQuest(String key) {
        return questWithTask(key, 0, 0, taskSamples().get("checkbox"));
    }
    
    public static JsonElement parse(String text) {
        return JsonParser.parseString(text);
    }
}
