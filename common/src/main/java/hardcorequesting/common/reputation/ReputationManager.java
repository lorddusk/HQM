package hardcorequesting.common.reputation;

import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.SimpleSerializable;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.quests.task.QuestTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReputationManager extends SimpleSerializable {
    public static final String FILE_PATH = "reputations.json";
    final Map<String, Reputation> reputationMap = new HashMap<>();
    
    public ReputationManager() {
        super();
    }
    
    public static ReputationManager getInstance() {
        return QuestLine.getActiveQuestLine().reputationManager;
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
    
    public void removeReputation(Reputation reputation) {
        for (Quest quest : Quest.getQuests().values()) {
            for (QuestTask<?> task : quest.getTasks()) {
                task.onRemovedReputation(reputation);
            }
        
            List<ReputationReward> rewards = quest.getRewards().getReputationRewards();
            if (rewards != null) {
                rewards.removeIf(reward -> reputation.equals(reward.getReward()));
            }
        }
    
        getReputations().remove(reputation.getId());
    }
    
    public int size() {
        return reputationMap.size();
    }
    
    @Override
    public String filePath() {
        return FILE_PATH;
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
    public void clear() {
        reputationMap.clear();
    }
    
    @Override
    public void loadFromString(String string) {
        SaveHandler.<List<Reputation>>load(string, new TypeToken<List<Reputation>>() {}.getType())
                .ifPresent(reputations -> reputations.forEach(this::addReputation));
    }
}
