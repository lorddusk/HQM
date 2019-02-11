package hardcorequesting.io;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.death.DeathStats;
import hardcorequesting.io.adapter.*;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.team.Team;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SaveHandler {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Reputation.class, ReputationAdapter.REPUTATION_ADAPTER)
            .registerTypeAdapter(QuestSet.class, QuestAdapter.QUEST_SET_ADAPTER)
            .registerTypeAdapter(GroupTier.class, BagAdapter.GROUP_TIER_ADAPTER)
            .registerTypeAdapter(DeathStats.class, DeathAdapter.DEATH_STATS_ADAPTER)
            .registerTypeAdapter(Team.class, TeamAdapter.TEAM_ADAPTER)
            .registerTypeAdapter(QuestingData.class, QuestingAdapter.QUESTING_DATA_ADAPTER)
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
            //.setPrettyPrinting() // TODO: make this dynamic
            .create();

    public static final Pattern JSON = Pattern.compile(".*\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern BAGS = Pattern.compile("^bags\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern DEATHS = Pattern.compile("^deaths\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern REPUTATIONS = Pattern.compile("^reputations\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern TEAMS = Pattern.compile("^teams\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern STATE = Pattern.compile("^state\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern DATA = Pattern.compile("^data\\.json$", Pattern.CASE_INSENSITIVE);
    public static final Pattern SETS = Pattern.compile("^sets\\.json$", Pattern.CASE_INSENSITIVE);
    public static final FileFilter QUEST_SET_FILTER =
            pathname -> JSON.matcher(pathname.getName()).find()
                    && !REPUTATIONS.matcher(pathname.getName()).find()
                    && !BAGS.matcher(pathname.getName()).find()
                    && !TEAMS.matcher(pathname.getName()).find()
                    && !STATE.matcher(pathname.getName()).find()
                    && !DATA.matcher(pathname.getName()).find()
                    && !SETS.matcher(pathname.getName()).find()
                    && !DEATHS.matcher(pathname.getName()).find();

    public static final String EXPORTS = "exports";
    public static final String REMOTE = "remote";
    public static final String DEFAULT = "default";
    private static final String QUESTING = "questing";
    private static final String HARDCORE = "hardcore";

    public static File getExportFile(String name) throws IOException {
        File file = new File(new File(HardcoreQuesting.configDir, EXPORTS), name.endsWith(".txt") ? name : name + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return file;
    }

    public static File getLocalFile(String name) throws IOException {
        File file = new File(new File(QuestLine.getActiveQuestLine().mainPath), name.endsWith(".txt") ? name : name + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return file;
    }

    public static File getRemoteFile(String name) throws IOException {
        File file = new File(new File(HardcoreQuesting.configDir, REMOTE), name.endsWith(".txt") ? name : name + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return file;
    }

    public static File getDefaultFile(String name) throws IOException {
        File file = new File(new File(HardcoreQuesting.configDir, DEFAULT), name.endsWith(".txt") ? name : name + ".json");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return file;
    }

    public static File getFile(String name, boolean remote) throws IOException{
        return remote ? getRemoteFile(name) : getLocalFile(name);
    }

    public static File getExportFolder() {
        return new File(HardcoreQuesting.configDir, EXPORTS);
    }

    public static File getLocalFolder() {
        return new File(QuestLine.getActiveQuestLine().mainPath);
    }

    public static File getRemoteFolder() {
        return new File(HardcoreQuesting.configDir, REMOTE);
    }

    public static File getDefaultFolder() {
        return new File(HardcoreQuesting.configDir, DEFAULT);
    }

    public static File getFolder(boolean remote){
        return remote ? getRemoteFolder() : getLocalFolder();
    }

    public static void copyFolder(File from, File to) {
        try {
            FileUtils.copyDirectory(from, to);
        } catch (IOException e) {
            HardcoreQuesting.LOG.info("Couldn't copy default files");
        }
    }

    public static void clearRemoteFolder() {
        if (getRemoteFolder().exists()) {
            getRemoteFolder().delete();
            getRemoteFolder().mkdirs();
        }
    }

    public static void removeFile(File file) {
        if (!file.exists()) return;
        file.delete();
    }

    public static void removeQuestSetFiles(File folder) {
        if (!folder.exists() || !folder.isDirectory()) return;
        for (File file : folder.listFiles(QUEST_SET_FILTER))
            removeFile(file);
    }

    public static void saveAllQuestSets(File folder) throws IOException {
        removeQuestSetFiles(folder);
        saveQuestSetList(Quest.getQuestSets(), new File(folder, "sets.json"));
        List<QuestSet> setsToSave = new ArrayList<>(); // possible fix for #251
        setsToSave.addAll(Quest.getQuestSets());
        for (QuestSet set : setsToSave) {
            saveQuestSet(set, new File(folder, set.getFilename() + ".json"));
        }
    }

    public static String saveAllQuestSets(List<String> names, List<String> questSets) {
        for (QuestSet set : Quest.getQuestSets()) {
            names.add(set.getFilename() + ".json");
            questSets.add(saveQuestSet(set));
        }
        return saveQuestSetList(Quest.getQuestSets());
    }

    public static void saveQuestSetList(List<QuestSet> sets, File file) throws IOException {
        if (!file.exists()) file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        JsonWriter writer = new JsonWriter(fileWriter);
        writer.beginArray();
        for (QuestSet set : sets)
            writer.value(set.getName());
        writer.endArray();
        writer.close();
    }

    public static String saveQuestSetList(List<QuestSet> sets) {
        StringWriter stringWriter = new StringWriter();
        try {
            JsonWriter writer = new JsonWriter(stringWriter);
            writer.beginArray();
            for (QuestSet set : sets)
                writer.value(set.getName());
            writer.endArray();
            writer.close();
        } catch (IOException ignored) {
        }
        return stringWriter.toString();
    }

    public static void saveQuestSet(QuestSet set, File file) throws IOException {
        save(file, set, new TypeToken<QuestSet>() {
        }.getType());
    }

    public static String saveQuestSet(QuestSet set) {
        return save(set, new TypeToken<QuestSet>() {
        }.getType());
    }

    public static List<String> loadQuestSetOrder(File file) throws IOException {
        List<String> order = new ArrayList<>();
        if (!file.exists()) return order;
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(new FileReader(file)).getAsJsonArray();
        for (JsonElement elem : array)
            order.add(elem.getAsString());
        return order;
    }

    public static void loadAllQuestSets(File folder) throws IOException {
        File[] files = folder.listFiles(QUEST_SET_FILTER);
        if (files != null)
            for (File file : files)
                loadQuestSet(file);
    }

    public static QuestSet loadQuestSet(File file) throws IOException {
        if (!file.exists()) return null;
        JsonReader reader = new JsonReader(new FileReader(file));
        QuestSet set = GSON.fromJson(reader, QuestSet.class);
        reader.close();
        return set;
    }

    public static void saveReputations(File file) throws IOException {
        save(file, Reputation.getReputationList(), new TypeToken<List<Reputation>>() {
        }.getType());
    }

    public static String saveReputations() {
        return save(Reputation.getReputationList(), new TypeToken<List<Reputation>>() {
        }.getType());
    }

    public static List<Reputation> loadReputations(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<Reputation> reputations = GSON.fromJson(reader, new TypeToken<List<Reputation>>() {
        }.getType());
        reader.close();
        return reputations == null ? new ArrayList<>() : reputations;
    }

    public static void saveTeams(File file) throws IOException {
        save(file, QuestingData.getTeams(), new TypeToken<List<Team>>() {
        }.getType());
    }

    public static String saveTeams() {
        return save(QuestingData.getTeams(), new TypeToken<List<Team>>() {
        }.getType());
    }

    public static String saveTeam(Team team) {
        return save(team, new TypeToken<Team>(){
        }.getType());
    }

    public static List<Team> loadTeams(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<Team> teams = GSON.fromJson(reader, new TypeToken<List<Team>>() {
        }.getType());
        reader.close();
        return teams == null ? new ArrayList<>() : teams;
    }

    public static void saveBags(File file) throws IOException {
        save(file, GroupTier.getTiers(), new TypeToken<List<GroupTier>>() {
        }.getType());
    }

    public static String saveBags() {
        return save(GroupTier.getTiers(), new TypeToken<List<GroupTier>>() {
        }.getType());
    }

    public static List<GroupTier> loadBags(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<GroupTier> bags = GSON.fromJson(reader, new TypeToken<List<GroupTier>>() {
        }.getType());
        reader.close();
        return bags == null ? new ArrayList<>() : bags;
    }

    public static void saveDeaths(File file) throws IOException {
        save(file, DeathStats.getDeathStatsList(), new TypeToken<List<DeathStats>>() {
        }.getType());
    }

    public static String saveDeaths() {
        return save(DeathStats.getDeathStatsList(), new TypeToken<List<DeathStats>>() {
        }.getType());
    }

    public static List<DeathStats> loadDeaths(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<DeathStats> deaths = GSON.fromJson(reader, new TypeToken<List<DeathStats>>() {
        }.getType());
        reader.close();
        return deaths == null ? new ArrayList<>() : deaths;
    }

    public static void saveQuestingData(File file) throws IOException {
        save(file, QuestingData.getData().values(), new TypeToken<List<QuestingData>>() {
        }.getType());
    }

    public static String saveQuestingData() {
        return save(QuestingData.getData().values(), new TypeToken<List<QuestingData>>() {
        }.getType());
    }

    public static String saveQuestingData(QuestingData questingData) {
        return save(questingData, new TypeToken<QuestingData>() {
        }.getType());
    }

    public static List<QuestingData> loadQuestingData(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<QuestingData> data = GSON.fromJson(reader, new TypeToken<List<QuestingData>>() {
        }.getType());
        reader.close();
        return data == null ? new ArrayList<>() : data;
    }

    public static File save(File file, Object object, Type type) throws IOException {
        if (!file.exists()) file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        SaveHandler.GSON.toJson(object, type, fileWriter);
        fileWriter.close();
        return file;
    }

    public static String save(Object object, Type type) {
        return SaveHandler.GSON.toJson(object, type);
    }

    public static void saveQuestingState(File file) throws IOException {
        if (!file.exists()) file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        JsonWriter writer = new JsonWriter(fileWriter);
        writer.beginObject();
        writer.name(QUESTING).value(QuestingData.isQuestActive());
        writer.name(HARDCORE).value(QuestingData.isHardcoreActive());
        writer.endObject();
        writer.close();
    }

    public static String saveQuestingState(boolean questing, boolean hardcore) throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name(QUESTING).value(questing);
        writer.name(HARDCORE).value(hardcore);
        writer.endObject();
        writer.close();
        return stringWriter.toString();
    }

    public static void loadQuestingState(File file) throws IOException {
        if (file.exists()) {
            JsonParser parser = new JsonParser();
            FileReader reader = new FileReader(file);
            JsonObject object = parser.parse(reader).getAsJsonObject();
            QuestingData.deactivate();
            if (object.get(QUESTING).getAsBoolean() || QuestingData.autoQuestActivate) QuestingData.activateQuest(false);
            if (object.get(HARDCORE).getAsBoolean() || QuestingData.autoHardcoreActivate) QuestingData.activateHardcore();
            reader.close();
        } else { // when there is no file use the defaults anyway
            if (QuestingData.autoQuestActivate) QuestingData.activateQuest(false);
            if (QuestingData.autoHardcoreActivate) QuestingData.activateHardcore();
        }
    }

    public static void saveDescription(File file, String description) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(description);
        writer.close();
    }

    public static String loadDescription(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
