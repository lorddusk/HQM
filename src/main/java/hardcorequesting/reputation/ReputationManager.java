package hardcorequesting.reputation;

import com.google.gson.reflect.TypeToken;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.SimpleSerializable;

import java.util.*;

public class ReputationManager extends SimpleSerializable {
    final Map<String, Reputation> reputationMap = new HashMap<>();
    
    public ReputationManager(QuestLine parent) {
        super(parent);
    }
    
    public static ReputationManager getInstance() {
        return QuestLine.getActiveQuestLine().reputationManager;
    }
    
    public void clear() {
        reputationMap.clear();
    }
    
    public Map<String, Reputation> getReputations() {
        return reputationMap;
    }
    
    public List<Reputation> getReputationList() {
        return new ArrayList<>(reputationMap.values());
    }
    
    public Reputation getReputation(String id) {
        return reputationMap.get(id);
    }
    
    public void addReputation(Reputation reputation) {
        reputationMap.put(reputation.getId(), reputation);
    }
    
    public int size() {
        return reputationMap.size();
    }
    
    @Override
    public String filePath() {
        return "reputations.json";
    }
    
    @Override
    public boolean isData() {
        return false;
    }
    
    @Override
    public String saveToString() {
        return SaveHandler.save(getReputationList(), new TypeToken<List<Reputation>>() {}.getType());
    }
    
    @Override
    public void loadFromString(Optional<String> string) {
        reputationMap.clear();
        string.flatMap(s -> SaveHandler.<List<Reputation>>load(s, new TypeToken<List<GroupTier>>() {}.getType()))
                .ifPresent(reputations -> reputations.forEach(this::addReputation));
    }
}
