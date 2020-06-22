package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskReputationKill;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class QuestTaskReputationKill extends QuestTaskReputation {
    
    private int kills;
    
    public QuestTaskReputationKill(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription, 20);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(MatrixStack matrices, GuiQuestBook gui, PlayerEntity player, int mX, int mY) {
        super.draw(matrices, gui, player, mX, mY);
        int killCount = ((QuestDataTaskReputationKill) getData(player)).kills;
        if (Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.pluralTranslated(kills != 1, "hqm.repKil.kills", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(matrices, gui.getLinesFromText(killCount == kills ? Translator.pluralTranslated(kills != 1, "hqm.repKil.killCount", GuiColor.GREEN, kills) : Translator.translated("hqm.repKil.killCountOutOf", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        }
    }
    
    @Override
    public float getCompletedRatio(UUID playerID) {
        return (float) ((QuestDataTaskReputationKill) getData(playerID)).kills / kills;
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        ((QuestDataTaskReputationKill) own).kills = Math.max(((QuestDataTaskReputationKill) own).kills, ((QuestDataTaskReputationKill) other).kills);
        
        if (((QuestDataTaskReputationKill) own).kills == kills) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        if (status) {
            this.kills = ((QuestDataTaskReputationKill) getData(playerID)).kills;
        } else {
            this.kills = 0;
        }
    }
    
    @Override
    protected PlayerEntity getPlayerForRender(PlayerEntity player) {
        return null;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskReputationKill.class;
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
        
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        
        ((QuestDataTaskReputationKill) own).kills = ((QuestDataTaskReputationKill) other).kills;
    }
    
    @Override
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        PlayerEntity killer = QuestTaskMob.getKiller(source);
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !this.isCompleted(killer) && !killer.equals(entity)) {
            if (entity instanceof PlayerEntity && isPlayerInRange((PlayerEntity) entity)) {
                QuestDataTaskReputationKill killData = (QuestDataTaskReputationKill) getData(killer);
                if (killData.kills < kills) {
                    killData.kills += 1;
                    
                    if (killData.kills == kills) {
                        completeTask(killer.getUuid());
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
}
