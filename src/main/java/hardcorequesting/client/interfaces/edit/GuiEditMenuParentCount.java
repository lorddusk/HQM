package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.quests.Quest;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class GuiEditMenuParentCount extends GuiEditMenuExtended {

    private boolean showModifiedParentRequirement;
    private int parentRequirementCount;
    private UUID questId;

    public GuiEditMenuParentCount(GuiBase gui, EntityPlayer player, Quest quest) {
        super(gui, player, true, 25, 20, 25, 105);

        this.questId = quest.getQuestId();
        this.parentRequirementCount = quest.getParentRequirementCount();
        this.showModifiedParentRequirement = quest.getUseModifiedParentRequirement();

        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.parentCount.count") {
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
        return Translator.translate("hqm.parentCount.req" + (showModifiedParentRequirement ? "Count" : "All") + ".title");
    }

    @Override
    protected String getArrowDescription() {
        return Translator.translate("hqm.parentCount.req" + (showModifiedParentRequirement ? "Count" : "All") + ".desc");
    }

    @Override
    public void save(GuiBase gui) {
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            quest.setParentRequirementCount(parentRequirementCount);
            SaveHelper.add(SaveHelper.EditType.PARENT_REQUIREMENT_CHANGED);
        }
    }
}
