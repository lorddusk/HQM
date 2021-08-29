package hardcorequesting.common.quests.data;


import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

public class QuestData {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public boolean[] reward;
    public boolean completed;
    public boolean claimed;
    private TaskData[] tasks;
    public boolean available = true;
    public long time;
    
    public QuestData(int players) {
        reward = new boolean[players];
    }
    
    
    public boolean getReward(Player player) {
        int id = getId(player);
        return (id >= 0 && id < reward.length) && reward[id];
    }
    
    public void claimReward(Player player) {
        int id = getId(player);
        if (id >= 0 && id < reward.length) {
            reward[id] = false;
        }
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
    
    @Deprecated //Refer to Quest instead
    public int getTaskSize() {
        return tasks.length;
    }
    
    @Nullable
    public TaskData getTaskData(int id) {
        if (id >= tasks.length)
            return null;
        else return tasks[id];
    }
    
    public <Data extends TaskData> Data getTaskData(int id, Class<Data> clazz, Supplier<Data> emptySupplier) {
        if (id >= tasks.length) {
            tasks = Arrays.copyOf(tasks, id + 1);
            Data newData = emptySupplier.get();
            tasks[id] = newData;
            return newData;
        }
    
        TaskData data = tasks[id];
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        } else {
            LOGGER.warn("Found task data of wrong type. Expected {}, was {}. Replacing with empty data of the correct type.", clazz, data == null ? null : data.getClass());
            Data newData = emptySupplier.get();
            tasks[id] = newData;
            return newData;
        }
    }
    
    public void clearTaskDataWithSize(int size) {
        tasks = new TaskData[size];
    }
    
    public void setTaskData(int id, TaskData data) {
        if (id >= tasks.length) {
            tasks = Arrays.copyOf(tasks, id + 1);
        }
        tasks[id] = data;
    }
    
    public void resetTaskData(int id, Supplier<? extends TaskData> emptySupplier) {
        if (id < tasks.length) {
            tasks[id] = emptySupplier.get();
        }
    }
}
