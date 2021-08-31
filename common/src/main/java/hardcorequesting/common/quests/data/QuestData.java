package hardcorequesting.common.quests.data;


import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class QuestData {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private final List<Boolean> claimableRewards = new ArrayList<>();
    private final List<TaskData> taskData = new ArrayList<>();
    public boolean completed;
    public boolean teamRewardClaimed;
    public boolean available = true;
    public long time;
    
    // Create new data
    public QuestData(int players) {
        clearRewardClaims(players);
    }
    
    // Initialize data from serialization
    public QuestData() {}
    
    public void setRewardsFromSerialization(List<Boolean> claimableRewards) {
        this.claimableRewards.clear();
        this.claimableRewards.addAll(claimableRewards);
    }
    
    public Iterable<Boolean> getRewardsForSerialization() {
        return claimableRewards;
    }
    
    public void setTaskDataFromSerialization(List<TaskData> taskData) {
        this.taskData.clear();
        this.taskData.addAll(taskData);
    }
    
    public Iterable<TaskData> getTaskDataForSerialization() {
        return taskData;
    }
    
    public void clearRewardClaims(int players) {
        claimableRewards.clear();
        for (int i = 0; i < players; i++)
            claimableRewards.add(false);
    }
    
    public boolean canClaimPlayerReward(int playerId) {
        return claimableRewards.get(playerId);
    }
    
    public void setCanClaimReward(int playerId, boolean canClaim) {
        claimableRewards.set(playerId, canClaim);
    }
    
    public void removePlayer(int playerId) {
        claimableRewards.remove(playerId);
    }
    
    public void insertPlayer(int playerId, boolean canClaim) {
        claimableRewards.add(playerId, canClaim);
    }
    
    public boolean canClaimPlayerReward(Player player) {
        int id = getId(player);
        return (id >= 0 && id < claimableRewards.size()) && claimableRewards.get(id);
    }
    
    public void claimReward(Player player) {
        int id = getId(player);
        if (id >= 0 && id < claimableRewards.size()) {
            claimableRewards.set(id, false);
        }
    }
    
    public void claimFullReward() {
        Collections.fill(claimableRewards, false);
    }
    
    public void unlockRewardForAll() {
        Collections.fill(claimableRewards, true);
    }
    
    public void unlockRewardForRandom() {
        int rewardId = (int) (Math.random() * claimableRewards.size());
        claimableRewards.set(rewardId, true);
    }
    
    private int getId(Player player) {
        Team team = QuestingDataManager.getInstance().getQuestingData(player).getTeam();
        int id = 0;
        for (PlayerEntry entry : team.getPlayers()) {
            if (entry.isInTeam()) {
                if (entry.getUUID().equals(player.getUUID())) {
                    return id;
                }
                id++;
            }
        }
        
        return -1;
    }
    
    public boolean canClaimTeamRewards() {
        return completed && !teamRewardClaimed;
    }
    
    public void verifyTasksSize(Quest quest) {
        while (taskData.size() < quest.getTasks().size()) {
            taskData.add(quest.getTasks().get(taskData.size()).newQuestData());
        }
    }
    
    public <Data extends TaskData> Data getTaskData(Quest quest, int id, Class<Data> clazz, Supplier<Data> emptySupplier) {
        verifyTasksSize(quest);
        
        TaskData data = taskData.get(id);
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        } else {
            LOGGER.warn("Found task data of wrong type. Expected {}, was {}. Replacing with empty data of the correct type.", clazz, data == null ? null : data.getClass());
            Data newData = emptySupplier.get();
            taskData.set(id, newData);
            return newData;
        }
    }
    
    public void clearTaskData(Quest quest) {
        taskData.clear();
        verifyTasksSize(quest);
    }
    
    public void resetTaskData(int id, Supplier<? extends TaskData> emptySupplier) {
        if (id < taskData.size()) {
            taskData.set(id, emptySupplier.get());
        }
    }
}