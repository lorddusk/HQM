package hardcorequesting.quests;


import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import net.minecraft.entity.player.EntityPlayer;

public class QuestData {

    public boolean[] reward;
    public boolean completed;
    public boolean claimed;
    public QuestDataTask[] tasks;
    public boolean available = true;
    public int time;

    public QuestData(int players) {
        reward = new boolean[players];
    }


    public boolean getReward(EntityPlayer player) {
        int id = getId(player);
        return (id >= 0 && id < reward.length) && reward[id];
    }

    public void claimReward(EntityPlayer player) {
        int id = getId(player);
        if (id >= 0 && id < reward.length) {
            reward[id] = false;
        }
    }

    private int getId(EntityPlayer player) {
        Team team = QuestingData.getQuestingData(player).getTeam();
        int id = 0;
        for (PlayerEntry entry : team.getPlayers()) {
            if (entry.isInTeam()) {
                if (entry.getUUID().equals(QuestingData.getUserUUID(player))) {
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
