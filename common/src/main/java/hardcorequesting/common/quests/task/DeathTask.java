package hardcorequesting.common.quests.task;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.DeathTaskData;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;


public class DeathTask extends QuestTask<DeathTaskData> {
    private static final String DEATHS = "deaths";
    private int deaths;
    
    public DeathTask(Quest parent, String description, String longDescription) {
        super(DeathTaskData.class, parent, description, longDescription);
        
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    public DeathTaskData newQuestData() {
        return new DeathTaskData();
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        int died = getData(player).getDeaths();
        FormattedText text = died == deaths
                ? Translator.pluralTranslated(deaths != 0, "hqm.deathMenu.deaths", GuiColor.GREEN, deaths)
                : Translator.pluralTranslated(deaths != 0, "hqm.deathMenu.deathsOutOf", died, deaths);
        
        gui.drawString(matrices, gui.getLinesFromText(text, 1F, 130), START_X, START_Y, 1F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        
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
            if (parent.isEnabled((Player) player) && parent.isAvailable((Player) player) && this.isVisible((Player) player) && !isCompleted((Player) player)) {
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
