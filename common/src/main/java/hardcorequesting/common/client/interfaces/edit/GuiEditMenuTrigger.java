package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.TriggerType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class GuiEditMenuTrigger extends GuiEditMenuExtended {
    
    private TriggerType type;
    private int triggerTasks;
    private UUID questId;
    
    public GuiEditMenuTrigger(GuiQuestBook gui, Player player, Quest quest) {
        super(gui, player, true, 25, 20);
        
        this.questId = quest.getQuestId();
        this.type = quest.getTriggerType();
        this.triggerTasks = quest.getTriggerTasks();
        
        textBoxes.add(new NumberTextBox(gui, 25, 135, "hqm.menuTrigger.taskCount") {
            @Override
            protected int getValue() {
                return triggerTasks;
            }
            
            @Override
            protected void setValue(int number) {
                triggerTasks = number;
            }
            
            @Override
            protected boolean isVisible() {
                return type.isUseTaskCount();
            }
        });
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        if (left) {
            type = TriggerType.values()[(type.ordinal() + TriggerType.values().length - 1) % TriggerType.values().length];
        } else {
            type = TriggerType.values()[(type.ordinal() + 1) % TriggerType.values().length];
        }
    }
    
    @Override
    protected String getArrowText() {
        return type.getName();
    }
    
    @Override
    protected String getArrowDescription() {
        return type.getDescription();
    }
    
    @Override
    public void save(GuiBase gui) {
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            quest.setTriggerType(type);
            quest.setTriggerTasks(Math.max(1, triggerTasks));
            SaveHelper.add(EditType.VISIBILITY_CHANGED);
        }
    }
    
}
