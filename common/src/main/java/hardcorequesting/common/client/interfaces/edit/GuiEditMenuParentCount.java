package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.client.resources.language.I18n;

import java.util.UUID;

public class GuiEditMenuParentCount extends GuiEditMenu {
    
    private boolean showModifiedParentRequirement;
    private int parentRequirementCount;
    private Quest quest;
    private final ArrowSelectionHelper selectionHelper;
    
    public GuiEditMenuParentCount(GuiQuestBook gui, UUID playerId, Quest quest) {
        super(gui, playerId, true);
    
        this.quest = quest;
        this.parentRequirementCount = quest._getParentRequirementCount();
        this.showModifiedParentRequirement = quest.getUseModifiedParentRequirement();
        if (!showModifiedParentRequirement) this.parentRequirementCount = quest.getRequirements().size();
        
        addTextBox(new NumberTextBox(gui, 25, 105, "hqm.parentCount.count") {
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
        
        selectionHelper = new ArrowSelectionHelper(gui,  25, 20) {
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
    public void onRelease(int mX, int mY, int button) {
        super.onRelease(mX, mY, button);
        
        selectionHelper.onRelease();
    }
    
    @Override
    public void save() {
        quest.setParentRequirementCount(showModifiedParentRequirement ? parentRequirementCount : -1);
        SaveHelper.add(EditType.PARENT_REQUIREMENT_CHANGED);
    }
}
