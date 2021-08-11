package hardcorequesting.common.quests.task;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskDeath;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;


public class DeathTask extends QuestTask {
    private static final String DEATHS = "deaths";
    private int deaths;
    
    public DeathTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskDeath.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        int died = ((QuestDataTaskDeath) getData(player)).deaths;
        gui.drawString(matrices, gui.getLinesFromText(Translator.plain(died == deaths ? GuiColor.GREEN + Translator.pluralTranslated(deaths != 0, "hqm.deathMenu.deaths", deaths).getString() : Translator.pluralTranslated(deaths != 0, "hqm.deathMenu.deathsOutOf", died, deaths).getString()), 1F, 130), START_X, START_Y, 1F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        
    }
    
    @Override
    public void onUpdate(Player player) {
        
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
        if (player instanceof ServerPlayer) {
            if (parent.isEnabled((Player) player) && parent.isAvailable((Player) player) && this.isVisible((Player) player) && !isCompleted((Player) player)) {
                QuestDataTaskDeath deathData = (QuestDataTaskDeath) getData((Player) player);
                if (deathData.deaths < deaths) {
                    deathData.deaths += 1;
                    
                    if (deathData.deaths == deaths) {
                        completeTask(player.getUUID());
                    }
                    
                    parent.sendUpdatedDataToTeam((Player) player);
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
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(DEATHS, getDeaths());
    }
    
    @Override
    public void read(JsonObject object) {
        setDeaths(GsonHelper.getAsInt(object, DEATHS, 0));
    }
}
