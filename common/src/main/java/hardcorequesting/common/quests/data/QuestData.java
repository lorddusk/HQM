package hardcorequesting.common.quests.data;


import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import net.minecraft.world.entity.player.Player;

public class QuestData {
    
    public boolean[] reward;
    public boolean completed;
    public boolean claimed;
    public TaskData[] tasks;
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
}
