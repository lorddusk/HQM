package hardcorequesting.quests;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.EventHandler;
import hardcorequesting.FileVersion;
import hardcorequesting.Translator;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
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

    @Override
    public void write(DataWriter dw, QuestDataTask task, boolean light) {
        super.write(dw, task, light);

        dw.writeData(((QuestDataTaskDeath) task).deaths, DataBitHelper.DEATHS);
    }

    @Override
    public void read(DataReader dr, QuestDataTask task, FileVersion version, boolean light) {
        super.read(dr, task, version, light);

        ((QuestDataTaskDeath) task).deaths = dr.readData(DataBitHelper.DEATHS);
    }

    @Override
    public void save(DataWriter dw) {
        dw.writeData(deaths, DataBitHelper.DEATHS);
    }

    @Override
    public void load(DataReader dr, FileVersion version) {
        deaths = dr.readData(DataBitHelper.DEATHS);
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
    public void onUpdate(EntityPlayer player, DataReader dr) {

    }

    @Override
    public float getCompletedRatio(String playerName) {
        return (float) ((QuestDataTaskDeath) getData(playerName)).deaths / deaths;
    }

    @Override
    public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
        ((QuestDataTaskDeath) own).deaths = Math.max(((QuestDataTaskDeath) own).deaths, ((QuestDataTaskDeath) other).deaths);

        if (((QuestDataTaskDeath) own).deaths == deaths) {
            completeTask(playerName);
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);

        ((QuestDataTaskDeath) own).deaths = ((QuestDataTaskDeath) other).deaths;
    }

    @Override
    public void autoComplete(String playerName) {
        deaths = ((QuestDataTaskDeath) getData(playerName)).deaths;
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
