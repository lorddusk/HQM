package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.TriggerType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.network.chat.FormattedText;

import java.util.UUID;

public class GuiEditMenuTrigger extends GuiEditMenu {
    
    private TriggerType type;
    private int triggerTasks;
    private UUID questId;
    
    public GuiEditMenuTrigger(GuiQuestBook gui, UUID playerId, Quest quest) {
        super(gui, playerId, true);
    
        this.questId = quest.getQuestId();
        this.type = quest.getTriggerType();
        this.triggerTasks = quest.getTriggerTasks();
        
        addTextBox(new NumberTextBox(gui, 25, 135, "hqm.menuTrigger.taskCount") {
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
        
        addClickable(new ArrowSelectionHelper(gui, 25, 20) {
    
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    type = TriggerType.values()[(type.ordinal() + TriggerType.values().length - 1) % TriggerType.values().length];
                } else {
                    type = TriggerType.values()[(type.ordinal() + 1) % TriggerType.values().length];
                }
            }
    
            @Override
            protected FormattedText getArrowText() {
                return type.getName();
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return type.getDescription();
            }
        });
    }
    
    @Override
    public void save() {
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            quest.setTriggerType(type);
            quest.setTriggerTasks(Math.max(1, triggerTasks));
            SaveHelper.add(EditType.VISIBILITY_CHANGED);
        }
    }
    
}
