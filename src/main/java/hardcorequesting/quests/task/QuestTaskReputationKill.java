package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskReputationKill;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class QuestTaskReputationKill extends QuestTaskReputation {

    private int kills;

    public QuestTaskReputationKill(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription, 20);

        register(EventTrigger.Type.DEATH);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        super.draw(gui, player, mX, mY);
        int killCount = ((QuestDataTaskReputationKill) getData(player)).kills;
        if (Quest.canQuestsBeEdited(player)) {
            gui.drawString(gui.getLinesFromText(Translator.translate(kills != 1, "hqm.repKil.kills", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(gui.getLinesFromText(killCount == kills ? GuiColor.GREEN + Translator.translate(kills != 1, "hqm.repKil.killCount", kills) : Translator.translate("hqm.repKil.killCountOutOf", killCount, kills), 1F, 130), START_X, START_Y, 1F, 0x404040);
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
    public void autoComplete(UUID playerID) {
        this.kills = ((QuestDataTaskReputationKill) getData(playerID)).kills;
    }

    @Override
    protected EntityPlayer getPlayerForRender(EntityPlayer player) {
        return null;
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskReputationKill.class;
    }

    @Override
    public void onUpdate(EntityPlayer player) {

    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);

        ((QuestDataTaskReputationKill) own).kills = ((QuestDataTaskReputationKill) other).kills;
    }

    @Override
    public void onLivingDeath(LivingDeathEvent event) {
        EntityPlayer killer = QuestTaskMob.getKiller(event);
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !this.isCompleted(killer) && !killer.equals(event.getEntityLiving())) {
            if (event.getEntityLiving() instanceof EntityPlayer && isPlayerInRange((EntityPlayer) event.getEntityLiving())) {
                QuestDataTaskReputationKill killData = (QuestDataTaskReputationKill) getData(killer);
                if (killData.kills < kills) {
                    killData.kills += 1;

                    if (killData.kills == kills) {
                        completeTask(killer.getUniqueID());
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
