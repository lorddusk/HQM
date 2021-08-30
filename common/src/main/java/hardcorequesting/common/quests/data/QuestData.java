package hardcorequesting.common.quests.data;


import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class QuestData {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private boolean[] reward;
    public boolean completed;
    public boolean claimed;
    private final List<TaskData> tasks = new ArrayList<>();
    public boolean available = true;
    public long time;
    
    public QuestData(int players) {
        clearRewardClaims(players);
    }
    
    @Deprecated
    public int getPlayers() {
        return reward.length;
    }
    
    public void clearRewardClaims(int players) {
        reward = new boolean[players];
    }
    
    public boolean canClaimReward(int playerId) {
        return reward[playerId];
    }
    
    public void setCanClaimReward(int playerId, boolean canClaim) {
        reward[playerId] = canClaim;
    }
    
    public void removePlayer(int playerId) {
        boolean[] old = reward;
        reward = new boolean[old.length - 1];
        for (int j = 0; j < reward.length; j++) {
            if (j < playerId) {
                reward[j] = old[j];
            } else {
                reward[j] = old[j + 1];
            }
        }
    }
    
    public void insertPlayer(int playerId, boolean canClaim) {
        boolean[] old = reward;
        reward = new boolean[old.length + 1];
        for (int j = 0; j < reward.length; j++) {
            if (j == playerId) {
                reward[j] = canClaim;
            } else if (j < playerId) {
                reward[j] = old[j];
            } else {
                reward[j] = old[j - 1];
            }
        }
    }
    
    public boolean canClaimReward(Player player) {
        int id = getId(player);
        return (id >= 0 && id < reward.length) && reward[id];
    }
    
    public void claimReward(Player player) {
        int id = getId(player);
        if (id >= 0 && id < reward.length) {
            reward[id] = false;
        }
    }
    
    public void claimFullReward() {
        Arrays.fill(reward, false);
    }
    
    public void unlockRewardForAll() {
        Arrays.fill(reward, true);
    }
    
    public void unlockRewardForRandom() {
        int rewardId = (int) (Math.random() * reward.length);
        reward[rewardId] = true;
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
    
    public boolean canClaim() {
        return completed && !claimed;
    }
    
    public void setTaskDataFromSerialization(List<TaskData> taskData) {
        tasks.clear();
        tasks.addAll(taskData);
    }
    
    public Iterable<TaskData> getTaskDataForSerialization() {
        return tasks;
    }
    
    public void verifyTasksSize(Quest quest) {
        while (tasks.size() < quest.getTasks().size()) {
            tasks.add(quest.getTasks().get(tasks.size()).newQuestData());
        }
    }
    
    public <Data extends TaskData> Data getTaskData(Quest quest, int id, Class<Data> clazz, Supplier<Data> emptySupplier) {
        verifyTasksSize(quest);
        
        TaskData data = tasks.get(id);
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        } else {
            LOGGER.warn("Found task data of wrong type. Expected {}, was {}. Replacing with empty data of the correct type.", clazz, data == null ? null : data.getClass());
            Data newData = emptySupplier.get();
            tasks.set(id, newData);
            return newData;
        }
    }
    
    public void clearTaskData(Quest quest) {
        tasks.clear();
        verifyTasksSize(quest);
    }
    
    public void resetTaskData(int id, Supplier<? extends TaskData> emptySupplier) {
        if (id < tasks.size()) {
            tasks.set(id, emptySupplier.get());
        }
    }
}