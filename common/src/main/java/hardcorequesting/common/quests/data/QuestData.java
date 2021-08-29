package hardcorequesting.common.quests.data;


import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class QuestData {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public boolean[] reward;
    public boolean completed;
    public boolean claimed;
    private final List<TaskData> tasks = new ArrayList<>();
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
        return tasks.size();
    }
    
    @Nullable
    public TaskData getTaskData(int id) {
        if (id >= tasks.size())
            return null;
        else return tasks.get(id);
    }
    
    public <Data extends TaskData> Data getTaskData(int id, Class<Data> clazz, Supplier<Data> emptySupplier) {
        if (id >= tasks.size()) {
            while (id > tasks.size())
                tasks.add(null);
            Data newData = emptySupplier.get();
            tasks.add(newData);
            return newData;
        }
    
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
    
    public void clearTaskData() {
        tasks.clear();
    }
    
    public void setTaskData(int id, TaskData data) {
        while (id >= tasks.size()) {
            tasks.add(null);
        }
        tasks.set(id, data);
    }
    
    public void resetTaskData(int id, Supplier<? extends TaskData> emptySupplier) {
        if (id < tasks.size()) {
            tasks.set(id, emptySupplier.get());
        }
    }
}