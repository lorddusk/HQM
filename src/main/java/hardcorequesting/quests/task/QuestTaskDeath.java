package hardcorequesting.quests.task;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.event.EventHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskDeath;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class QuestTaskDeath extends QuestTask {
    private int deaths;


    public QuestTaskDeath(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventHandler.Type.DEATH);
    }

    @Override
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.entityLiving instanceof EntityPlayerMP) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            if (parent.isEnabled(player) && parent.isAvailable(player) && this.isVisible(player) && !isCompleted(player)) {
                QuestDataTaskDeath deathData = (QuestDataTaskDeath) getData(player);
                if (deathData.deaths < deaths) {
                    deathData.deaths += 1;

                    if (deathData.deaths == deaths) {
                        completeTask(player.getGameProfile().getName());
                    }

                    parent.sendUpdatedDataToTeam(player);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        int died = ((QuestDataTaskDeath) getData(player)).deaths;
        gui.drawString(gui.getLinesFromText(died == deaths ? GuiColor.GREEN + Translator.translate(deaths != 0, "hqm.deathMenu.deaths", deaths) : Translator.translate(deaths != 0, "hqm.deathMenu.deathsOutOf", died, deaths), 1F, 130), START_X, START_Y, 1F, 0x404040);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {

    }

    @Override
    public float getCompletedRatio(String uuid) {
        return (float) ((QuestDataTaskDeath) getData(uuid)).deaths / deaths;
    }

    @Override
    public void mergeProgress(String uuid, QuestDataTask own, QuestDataTask other) {
        ((QuestDataTaskDeath) own).deaths = Math.max(((QuestDataTaskDeath) own).deaths, ((QuestDataTaskDeath) other).deaths);

        if (((QuestDataTaskDeath) own).deaths == deaths) {
            completeTask(uuid);
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);

        ((QuestDataTaskDeath) own).deaths = ((QuestDataTaskDeath) other).deaths;
    }

    @Override
    public void autoComplete(String uuid) {
        deaths = ((QuestDataTaskDeath) getData(uuid)).deaths;
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskDeath.class;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
