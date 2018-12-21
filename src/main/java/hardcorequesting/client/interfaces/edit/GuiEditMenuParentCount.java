package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.quests.Quest;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class GuiEditMenuParentCount extends GuiEditMenuExtended {

    private boolean useModifiedParentRequirement;
    private int parentRequirementCount;
    private UUID questId;

    public GuiEditMenuParentCount(GuiBase gui, EntityPlayer player, Quest quest) {
        super(gui, player, true, 25, 20, 25, 105);

        this.questId = quest.getQuestId();
        this.useModifiedParentRequirement = quest.getUseModifiedParentRequirement();
        if (useModifiedParentRequirement) {
            this.parentRequirementCount = quest.getParentRequirementCount();
        } else {
            this.parentRequirementCount = quest.getRequirements().size();
        }


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
                return useModifiedParentRequirement;
            }
        });
    }

    @Override
    protected void onArrowClick(boolean left) {
        useModifiedParentRequirement = !useModifiedParentRequirement;
    }

    @Override
    protected String getArrowText() {
        return Translator.translate("hqm.parentCount.req" + (useModifiedParentRequirement ? "Count" : "All") + ".title");
    }

    @Override
    protected String getArrowDescription() {
        return Translator.translate("hqm.parentCount.req" + (useModifiedParentRequirement ? "Count" : "All") + ".desc");
    }

    @Override
    public void save(GuiBase gui) {
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            quest.setUseModifiedParentRequirement(useModifiedParentRequirement);
            quest.setParentRequirementCount(parentRequirementCount);
            SaveHelper.add(SaveHelper.EditType.PARENT_REQUIREMENT_CHANGED);
        }
    }
}
