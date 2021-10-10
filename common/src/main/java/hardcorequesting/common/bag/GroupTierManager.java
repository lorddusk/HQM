package hardcorequesting.common.bag;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.SimpleSerializable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GroupTierManager extends SimpleSerializable {
    public static final String FILE_PATH = "bags.json";
    public final Map<UUID, Group> groups = new ConcurrentHashMap<>();
    private final List<GroupTier> tiers = Lists.newArrayList();
    
    public GroupTierManager() {
        super();
    }
    
    public static GroupTierManager getInstance() {
        return QuestLine.getActiveQuestLine().groupTierManager;
    }
    
    @Override
    public String saveToString() {
        return SaveHandler.save(tiers, new TypeToken<List<GroupTier>>() {}.getType());
    }
    
    @Override
    public void loadFromString(Optional<String> string) {
        groups.clear();
        tiers.clear();
        string.flatMap(s -> SaveHandler.<List<GroupTier>>load(s, new TypeToken<List<GroupTier>>() {}.getType())).ifPresent(tiers::addAll);
    }
    
    @Override
    public String filePath() {
        return FILE_PATH;
    }
    
    @Override
    public boolean isData() {
        return false;
    }
    
    public List<GroupTier> getTiers() {
        return tiers;
    }
}
