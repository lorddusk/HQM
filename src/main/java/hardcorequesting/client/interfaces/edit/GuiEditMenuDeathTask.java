package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.quests.task.QuestTaskDeath;
import hardcorequesting.util.SaveHelper;
import net.minecraft.world.entity.player.Player;


public class GuiEditMenuDeathTask extends GuiEditMenuExtended {
    
    private int deaths;
    private QuestTaskDeath task;
    
    public GuiEditMenuDeathTask(GuiBase gui, Player player, QuestTaskDeath task) {
        super(gui, player, true, -1, -1, 25, 30);
        
        deaths = task.getDeaths();
        this.task = task;
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.deathTask.reqDeathCount") {
            @Override
            protected int getValue() {
                return deaths;
            }
            
            @Override
            protected void setValue(int number) {
                deaths = number;
            }
        });
    }
    
    @Override
    public void save(GuiBase gui) {
        task.setDeaths(deaths);
        SaveHelper.add(SaveHelper.EditType.DEATH_CHANGE);
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
