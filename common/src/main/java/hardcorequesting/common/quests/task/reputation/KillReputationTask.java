package hardcorequesting.common.quests.task.reputation;

import com.google.gson.JsonObject;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.ReputationKillTaskData;
import hardcorequesting.common.quests.task.client.KillReputationTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class KillReputationTask extends ReputationTask<ReputationKillTaskData> {
    private static final String KILLS = "kills";
    private int kills;
    
    public KillReputationTask(Quest parent, String description, String longDescription) {
        super(ReputationKillTaskData.class, parent, description, longDescription);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected TaskGraphic createGraphic() {
        return new KillReputationTaskGraphic(this);
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        return (float) getData(team).kills / kills;
    }
    
    @Override
    public void mergeProgress(UUID playerID, ReputationKillTaskData own, ReputationKillTaskData other) {
        own.kills = Math.max(own.kills, other.kills);
        
        if (own.kills == kills) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void setComplete(ReputationKillTaskData data) {
        data.kills = kills;
        super.setComplete(data);
    }
    
    @Override
    public ReputationKillTaskData newQuestData() {
        return new ReputationKillTaskData();
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public void copyProgress(ReputationKillTaskData own, ReputationKillTaskData other) {
        super.copyProgress(own, other);
        
        own.kills = other.kills;
    }
    
    @Override
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        Player killer = KillMobsTask.getKiller(source);
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !this.isCompleted(killer) && !killer.equals(entity)) {
            if (entity instanceof Player && isPlayerInRange((Player) entity)) {
                ReputationKillTaskData killData = getData(killer);
                if (killData.kills < kills) {
                    killData.kills += 1;
                    
                    if (killData.kills == kills) {
                        completeTask(killer.getUUID());
                    }
                    
                    parent.sendUpdatedDataToTeam(killer);
                }
            }
        }
    }
    
    public int getKills(UUID id) {
        return getData(id).kills;
    }
    
    public int getKillsRequirement() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
        SaveHelper.add(EditType.KILLS_CHANGE);
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(KILLS, getKillsRequirement());
    }
    
    @Override
    public void read(JsonObject object) {
        super.read(object);
        kills = GsonHelper.getAsInt(object, KILLS, 0);
    }
}
