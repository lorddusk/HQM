package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.quests.task.QuestTaskReputationKill;
import hardcorequesting.util.SaveHelper;
import net.minecraft.entity.player.EntityPlayer;

public class GuiEditMenuReputationKillTask extends GuiEditMenuExtended {
    private int kills;
    private QuestTaskReputationKill task;

    public GuiEditMenuReputationKillTask(GuiBase gui, EntityPlayer player, QuestTaskReputationKill task) {
        super(gui, player, true, -1, -1, 25, 30);

        kills = task.getKills();
        this.task = task;

        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.mobTask.reqKills") {
            @Override
            protected void setValue(int number) {
                kills = number;
            }

            @Override
            protected int getValue() {
                return kills;
            }
        });
    }

    @Override
    public void save(GuiBase gui) {
        task.setKills(kills);
        SaveHelper.add(SaveHelper.EditType.KILLS_CHANGE);
    }

    @Override
    protected void onArrowClick(boolean left) {

    }

    @Override
    protected String getArrowText() {
        return null;
    }

    @Override
    protected String getArrowDescription() {
        return null;
    }
}
