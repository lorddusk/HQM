package hardcorequesting.quests;

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
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class QuestTaskReputationKill extends QuestTaskReputation {
    public QuestTaskReputationKill(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription, 20);

        register(EventHandler.Type.DEATH);
    }

    private int kills;

    @Override
    public void onLivingDeath(LivingDeathEvent event) {
        EntityPlayer killer = QuestTaskMob.getKiller(event);
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !this.isCompleted(killer) && !killer.equals(event.entityLiving)) {
            if (event.entityLiving instanceof EntityPlayer && isPlayerInRange((EntityPlayer) event.entityLiving)) {
                QuestDataTaskReputationKill killData = (QuestDataTaskReputationKill) getData(killer);
                if (killData.kills < kills) {
                    killData.kills += 1;

                    if (killData.kills == kills) {
                        completeTask(killer.getGameProfile().getName());
                    }

                    parent.sendUpdatedDataToTeam(killer);
                }
            }
        }
    }

    @Override
    protected EntityPlayer getPlayerForRender(EntityPlayer player) {
        return null;
    }

    @Override
    public void write(DataWriter dw, QuestDataTask task, boolean light) {
        super.write(dw, task, light);

        dw.writeData(((QuestDataTaskReputationKill) task).kills, DataBitHelper.DEATHS);
    }

    @Override
    public void read(DataReader dr, QuestDataTask task, FileVersion version, boolean light) {
        super.read(dr, task, version, light);

        ((QuestDataTaskReputationKill) task).kills = dr.readData(DataBitHelper.DEATHS);
    }

    @Override
    public void save(DataWriter dw) {
        super.save(dw);
        dw.writeData(kills, DataBitHelper.DEATHS);
    }

    @Override
    public void load(DataReader dr, FileVersion version) {
        super.load(dr, version);
        kills = dr.readData(DataBitHelper.DEATHS);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        super.draw(gui, player, mX, mY);
        int killCount = ((QuestDataTaskReputationKill) getData(player)).kills;
        if (Quest.isEditing) {
            gui.drawString(gui.getLinesFromText(Translator.translate(kills != 1, "hqm.repKil.kills", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(gui.getLinesFromText(killCount == kills ? GuiColor.GREEN + Translator.translate(kills != 1, "hqm.repKil.killCount", kills) : Translator.translate("hqm.repKil.killCountOutOf", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        }
    }

    @Override
    public void onUpdate(EntityPlayer player, DataReader dr) {

    }

    @Override
    public float getCompletedRatio(String playerName) {
        return (float) ((QuestDataTaskReputationKill) getData(playerName)).kills / kills;
    }

    @Override
    public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
        ((QuestDataTaskReputationKill) own).kills = Math.max(((QuestDataTaskReputationKill) own).kills, ((QuestDataTaskReputationKill) other).kills);

        if (((QuestDataTaskReputationKill) own).kills == kills) {
            completeTask(playerName);
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);

        ((QuestDataTaskReputationKill) own).kills = ((QuestDataTaskReputationKill) other).kills;
    }

    @Override
    public void autoComplete(String playerName) {
        kills = ((QuestDataTaskReputationKill) getData(playerName)).kills;
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskReputationKill.class;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }
}
