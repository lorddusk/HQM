package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.TriggerType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;

import java.util.UUID;

public class GuiEditMenuTrigger extends GuiEditMenuExtended {
    
    private TriggerType type;
    private int triggerTasks;
    private UUID questId;
    private final ArrowSelectionHelper selectionHelper;
    
    public GuiEditMenuTrigger(GuiQuestBook gui, UUID playerId, Quest quest) {
        super(gui, playerId, true);
        
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
        
        selectionHelper = new ArrowSelectionHelper(gui, 25, 20) {
    
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
        };
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        selectionHelper.render(matrices, mX, mY);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        selectionHelper.onClick(mX, mY);
    }
    
    @Override
    public void onRelease(int mX, int mY) {
        super.onRelease(mX, mY);
        
        selectionHelper.onRelease();
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
