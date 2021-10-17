package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

public class GuiEditMenuParentCount extends GuiEditMenu {
    
    private boolean showModifiedParentRequirement;
    private int parentRequirementCount;
    private Quest quest;
    
    public GuiEditMenuParentCount(GuiQuestBook gui, Quest quest) {
        super(gui, true);
    
        this.quest = quest;
        this.parentRequirementCount = quest._getParentRequirementCount();
        this.showModifiedParentRequirement = quest.getUseModifiedParentRequirement();
        if (!showModifiedParentRequirement) this.parentRequirementCount = quest.getRequirements().size();
        
        addTextBox(new NumberTextBox(gui, 25, 105, Translator.translatable("hqm.parentCount.count"),
                () -> parentRequirementCount, value -> parentRequirementCount = value) {
            @Override
            protected boolean isVisible() {
                return showModifiedParentRequirement;
            }
        });
        
        addClickable(new ArrowSelectionHelper(gui,  25, 20) {
            @Override
            protected void onArrowClick(boolean left) {
                showModifiedParentRequirement = !showModifiedParentRequirement;
            }
    
            @Override
            protected FormattedText getArrowText() {
                return Translator.translatable("hqm.parentCount.req" + (showModifiedParentRequirement ? "Count" : "All") + ".title");
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return Translator.translatable("hqm.parentCount.req" + (showModifiedParentRequirement ? "Count" : "All") + ".desc");
            }
    
        });
    }
    
    @Override
    public void save() {
        quest.setParentRequirementCount(showModifiedParentRequirement ? parentRequirementCount : -1);
        SaveHelper.add(EditType.PARENT_REQUIREMENT_CHANGED);
    }
}
