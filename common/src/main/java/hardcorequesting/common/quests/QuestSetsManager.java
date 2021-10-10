package hardcorequesting.common.quests;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.DataReader;
import hardcorequesting.common.io.DataWriter;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.io.adapter.QuestAdapter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class QuestSetsManager implements Serializable {
    private static final Pattern JSON = Pattern.compile(".*\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BAGS = Pattern.compile("^bags\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEATHS = Pattern.compile("^deaths\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPUTATIONS = Pattern.compile("^reputations\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TEAMS = Pattern.compile("^teams\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern STATE = Pattern.compile("^state\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA = Pattern.compile("^data\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SETS = Pattern.compile("^sets\\.json$", Pattern.CASE_INSENSITIVE);
    private static final DirectoryStream.Filter<Path> QUEST_SET_FILTER = path -> {
        String name = path.getFileName().toString();
        return JSON.matcher(name).find()
                && !REPUTATIONS.matcher(name).find()
                && !BAGS.matcher(name).find()
                && !TEAMS.matcher(name).find()
                && !STATE.matcher(name).find()
                && !DATA.matcher(name).find()
                && !SETS.matcher(name).find()
                && !DEATHS.matcher(name).find();
    };
    
    public final Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    public final List<QuestSet> questSets = Lists.newArrayList();
    
    public QuestSetsManager() {
    }
    
    public static QuestSetsManager getInstance() {
        return QuestLine.getActiveQuestLine().questSetsManager;
    }
    
    @Override
    public boolean isData() {
        return false;
    }
    
    @Override
    public void save(DataWriter writer) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        questSets.stream().map(QuestSet::getFilename).distinct().forEach(array::add);
        object.add("sets", array);
        writer.write("sets.json", object.toString());
        
        for (QuestSet set : questSets) {
            writer.write("sets/" + set.getFilename() + ".json", SaveHandler.save(set, QuestSet.class));
        }
    }
    
    @Override
    public void load(DataReader reader) {
        quests.clear();
        questSets.clear();
        EventTrigger.instance().clear();
        
        reader.read("sets.json")
                .map(SaveHandler.JSON_PARSER::parse)
                .ifPresent(jsonElement -> {
                    if (jsonElement.isJsonArray()) {
                        List<String> order = Lists.newArrayList();
                        for (JsonElement element : jsonElement.getAsJsonArray()) {
                            order.add(element.getAsString());
                        }
                        reader.readAll(QUEST_SET_FILTER)
                                .flatMap(setText -> SaveHandler.load(setText, QuestSet.class).stream())
                                .filter(Predicates.not(questSets::contains))
                                .forEach(questSets::add);
                        questSets.sort((s1, s2) -> {
                            if (s1.equals(s2)) return 0;
                            int is1 = order.indexOf(s1.getName());
                            int is2 = order.indexOf(s2.getName());
                            if (is1 == -1) {
                                return is2 == -1 ? s1.getName().compareTo(s2.getName()) : 1;
                            }
                            if (is2 == -1) return -1;
                            if (is1 == is2) return 0;
                            return is1 < is2 ? -1 : 1;
                        });
                    } else {
                        List<String> sets = Lists.newArrayList();
                        for (JsonElement element : jsonElement.getAsJsonObject().get("sets").getAsJsonArray()) {
                            sets.add(element.getAsString());
                        }
                        for (String set : sets) {
                            reader.read("sets/" + set + ".json")
                                    .flatMap(setText -> SaveHandler.load(setText, QuestSet.class))
                                    .filter(Predicates.not(questSets::contains))
                                    .ifPresent(questSets::add);
                        }
                    }
                });
        try {
            QuestAdapter.postLoad();
        } catch (IOException e) {
            HardcoreQuestingCore.LOGGER.warn("Failed loading quest sets for remote", e);
        }
        HardcoreQuestingCore.LOGGER.info("Loaded %d quests from %d quest sets.", quests.size(), questSets.size());
    }
}
