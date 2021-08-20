package hardcorequesting.common.quests.task;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.ReputationKillTaskData;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.Translator;
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
        super(ReputationKillTaskData.class, parent, description, longDescription, 20);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        super.draw(matrices, gui, player, mX, mY);
        int killCount = getData(player).kills;
        if (Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.pluralTranslated(kills != 1, "hqm.repKil.kills", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(matrices, gui.getLinesFromText(killCount == kills ? Translator.pluralTranslated(kills != 1, "hqm.repKil.killCount", GuiColor.GREEN, kills) : Translator.translatable("hqm.repKil.killCountOutOf", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        }
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
    protected Player getPlayerForRender(Player player) {
        return null;
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
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        super.write(builder);
        builder.add(KILLS, getKills());
    }
    
    @Override
    public void read(JsonObject object) {
        super.read(object);
        setKills(GsonHelper.getAsInt(object, KILLS, 0));
    }
}
