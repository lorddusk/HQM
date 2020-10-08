package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.quests.task.QuestTaskReputationKill;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.world.entity.player.Player;

public class GuiEditMenuReputationKillTask extends GuiEditMenuExtended {
    
    private int kills;
    private QuestTaskReputationKill task;
    
    public GuiEditMenuReputationKillTask(GuiBase gui, Player player, QuestTaskReputationKill task) {
        super(gui, player, true, -1, -1, 25, 30);
        
        kills = task.getKills();
        this.task = task;
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.mobTask.reqKills") {
            @Override
            protected int getValue() {
                return kills;
            }
            
            @Override
            protected void setValue(int number) {
                kills = number;
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
