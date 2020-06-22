package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskDeath;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.StringRenderable;

import java.util.UUID;


public class QuestTaskDeath extends QuestTask {
    
    private int deaths;
    
    
    public QuestTaskDeath(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskDeath.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(MatrixStack matrices, GuiQuestBook gui, PlayerEntity player, int mX, int mY) {
        int died = ((QuestDataTaskDeath) getData(player)).deaths;
        gui.drawString(matrices, gui.getLinesFromText(Translator.plain(died == deaths ? GuiColor.GREEN + Translator.translatable(deaths != 0, "hqm.deathMenu.deaths", deaths).getString() : Translator.translatable(deaths != 0, "hqm.deathMenu.deathsOutOf", died, deaths).getString()), 1F, 130), START_X, START_Y, 1F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, PlayerEntity player, int mX, int mY, int b) {
        
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
        
    }
    
    @Override
    public float getCompletedRatio(UUID playerID) {
        return (float) ((QuestDataTaskDeath) getData(playerID)).deaths / deaths;
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        ((QuestDataTaskDeath) own).deaths = Math.max(((QuestDataTaskDeath) own).deaths, ((QuestDataTaskDeath) other).deaths);
        
        if (((QuestDataTaskDeath) own).deaths == deaths) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        if (status) {
            deaths = ((QuestDataTaskDeath) getData(playerID)).deaths;
        } else {
            deaths = 0;
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        
        ((QuestDataTaskDeath) own).deaths = ((QuestDataTaskDeath) other).deaths;
    }
    
    @Override
    public void onLivingDeath(LivingEntity player, DamageSource source) {
        if (player instanceof ServerPlayerEntity) {
            if (parent.isEnabled((PlayerEntity) player) && parent.isAvailable((PlayerEntity) player) && this.isVisible((PlayerEntity) player) && !isCompleted((PlayerEntity) player)) {
                QuestDataTaskDeath deathData = (QuestDataTaskDeath) getData((PlayerEntity) player);
                if (deathData.deaths < deaths) {
                    deathData.deaths += 1;
                    
                    if (deathData.deaths == deaths) {
                        completeTask(player.getUuid());
                    }
                    
                    parent.sendUpdatedDataToTeam((PlayerEntity) player);
                }
            }
        }
    }
    
    @Override
    public void uncomplete(UUID playerId) {
        super.uncomplete(playerId);
        
        ((QuestDataTaskDeath) getData(playerId)).deaths = 0;
    }
    
    @Override
    public void completeTask(UUID uuid) {
        super.completeTask(uuid);
        ((QuestDataTaskDeath) getData(uuid)).deaths = deaths;
        completeQuest(parent, uuid);
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
