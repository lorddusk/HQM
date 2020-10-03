package hardcorequesting.common.io;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.death.DeathStat;
import hardcorequesting.common.io.adapter.*;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.team.Team;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaveHandler {
    
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Reputation.class, ReputationAdapter.REPUTATION_ADAPTER)
            .registerTypeAdapter(QuestSet.class, QuestAdapter.QUEST_SET_ADAPTER)
            .registerTypeAdapter(GroupTier.class, BagAdapter.GROUP_TIER_ADAPTER)
            .registerTypeAdapter(DeathStat.class, DeathAdapter.DEATH_STATS_ADAPTER)
            .registerTypeAdapter(Team.class, TeamAdapter.TEAM_ADAPTER)
            .registerTypeAdapter(QuestingData.class, QuestingAdapter.QUESTING_DATA_ADAPTER)
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
            .setPrettyPrinting()
            .create();
    
    public static final JsonParser JSON_PARSER = new JsonParser();
    
    public static final String QUESTING = "questing";
    public static final String HARDCORE = "hardcore";
    
    public static void copyFolder(File from, File to) {
        try {
            FileUtils.copyDirectory(from, to);
        } catch (IOException e) {
            HardcoreQuestingCore.LOGGER.info("Couldn't copy default files");
        }
    }
    
    public static <T> Optional<T> load(File file, Type type) {
        return load(file.toPath(), type);
    }
    
    public static <T> Optional<T> load(Path file, Type type) {
        if (!Files.exists(file)) return Optional.empty();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            Object o = GSON.fromJson(reader, type);
            return (Optional<T>) Optional.ofNullable(o);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public static <T> Optional<T> load(String str, Type type) {
        Object o = GSON.fromJson(str, type);
        return (Optional<T>) Optional.ofNullable(o);
    }
    
    public static <T> Optional<T> load(File file, Class<T> type) {
        return load(file.toPath(), type);
    }
    
    public static <T> Optional<T> load(Path file, Class<T> type) {
        if (!Files.exists(file)) return Optional.empty();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            Object o = GSON.fromJson(reader, type);
            return (Optional<T>) Optional.ofNullable(o);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public static <T> Optional<T> load(String str, Class<T> type) {
        Object o = GSON.fromJson(str, type);
        return (Optional<T>) Optional.ofNullable(o);
    }
    
    public static Optional<String> load(File file) {
        return load(file.toPath());
    }
    
    public static Optional<String> load(Path file) {
        if (!Files.exists(file)) return Optional.empty();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return Optional.ofNullable(IOUtils.toString(reader));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    public static String saveTeam(Team team) {
        return save(team, new TypeToken<Team>() {}.getType());
    }
    
    public static List<GroupTier> loadBags(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<GroupTier> bags = GSON.fromJson(reader, new TypeToken<List<GroupTier>>() {
        }.getType());
        reader.close();
        return bags == null ? new ArrayList<>() : bags;
    }
    
    public static void save(Path file, Object object, Type type) {
        save(file.toAbsolutePath().toFile(), save(object, type));
    }
    
    public static void save(File file, Object object, Type type) {
        save(file, save(object, type));
    }
    
    public static void save(Path file, String s) {
        save(file.toAbsolutePath().toFile(), s);
    }
    
    public static void save(File file, String s) {
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try (BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            fileWriter.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String save(Object object, Type type) {
        return SaveHandler.GSON.toJson(object, type);
    }
}
