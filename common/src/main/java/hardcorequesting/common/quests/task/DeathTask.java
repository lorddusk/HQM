package hardcorequesting.common.quests.task;

import com.google.gson.JsonObject;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.DeathTaskData;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;


public class DeathTask extends QuestTask<DeathTaskData> {
    private static final String DEATHS = "deaths";
    private int deaths;
    
    public DeathTask(Quest parent) {
        super(TaskType.DEATH.get(), DeathTaskData.class, parent);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    public DeathTaskData newQuestData() {
        return new DeathTaskData();
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        return (float) getData(team).getDeaths() / deaths;
    }
    
    @Override
    public void mergeProgress(UUID playerID, DeathTaskData own, DeathTaskData other) {
        own.merge(other);
        
        if (own.getDeaths() == deaths) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void setComplete(DeathTaskData data) {
        data.setDeaths(deaths);
        data.completed = true;
    }
    
    @Override
    public void copyProgress(DeathTaskData own, DeathTaskData other) {
        own.update(other);
    }
    
    @Override
    public void onLivingDeath(LivingEntity player, DamageSource source) {
        if (player instanceof ServerPlayer) {
            if (parent.isEnabled((Player) player) && parent.isAvailable((Player) player) && this.isVisible(player.getUUID()) && !isCompleted((Player) player)) {
                DeathTaskData deathData = getData((Player) player);
                if (deathData.getDeaths() < deaths) {
                    deathData.setDeaths(deathData.getDeaths() + 1);
                    
                    if (deathData.getDeaths() == deaths) {
                        completeTask(player.getUUID());
                    }
                    
                    parent.sendUpdatedDataToTeam((Player) player);
                }
            }
        }
    }
    
    @Override
    public void completeTask(UUID uuid) {
        super.completeTask(uuid);
        getData(uuid).setDeaths(deaths);
        completeQuest(parent, uuid);
    }
    
    public int getDeaths(UUID id) {
        return getData(id).getDeaths();
    }
    
    public int getDeathsRequired() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
        SaveHelper.add(EditType.DEATH_CHANGE);
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(DEATHS, getDeathsRequired());
    }
    
    @Override
    public void read(JsonObject object) {
        deaths = GsonHelper.getAsInt(object, DEATHS, 0);
    }
}
