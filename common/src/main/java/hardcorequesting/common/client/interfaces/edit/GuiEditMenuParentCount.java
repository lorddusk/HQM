package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.client.resources.language.I18n;

import java.util.UUID;

public class GuiEditMenuParentCount extends GuiEditMenuExtended {
    
    private boolean showModifiedParentRequirement;
    private int parentRequirementCount;
    private Quest quest;
    
    public GuiEditMenuParentCount(GuiBase gui, UUID playerId, Quest quest) {
        super(gui, playerId, true, 25, 20);
        
        this.quest = quest;
        this.parentRequirementCount = quest._getParentRequirementCount();
        this.showModifiedParentRequirement = quest.getUseModifiedParentRequirement();
        if (!showModifiedParentRequirement) this.parentRequirementCount = quest.getRequirements().size();
        
        textBoxes.add(new NumberTextBox(gui, 25, 105, "hqm.parentCount.count") {
            @Override
            protected int getValue() {
                return parentRequirementCount;
            }
            
            @Override
            protected void setValue(int number) {
                parentRequirementCount = number;
            }
            
            @Override
            protected boolean isVisible() {
                return showModifiedParentRequirement;
            }
        });
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        showModifiedParentRequirement = !showModifiedParentRequirement;
    }
    
    @Override
    protected String getArrowText() {
        return I18n.get("hqm.parentCount.req" + (showModifiedParentRequirement ? "Count" : "All") + ".title");
    }
    
    @Override
    protected String getArrowDescription() {
        return I18n.get("hqm.parentCount.req" + (showModifiedParentRequirement ? "Count" : "All") + ".desc");
    }
    
    @Override
    public void save() {
        quest.setParentRequirementCount(showModifiedParentRequirement ? parentRequirementCount : -1);
        SaveHelper.add(EditType.PARENT_REQUIREMENT_CHANGED);
    }
}
