package hardcorequesting.client.interfaces;

import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.quests.Quest;
import net.minecraft.entity.player.EntityPlayer;

public class GuiEditMenuParentCount extends GuiEditMenuExtended {
    private boolean useModifiedParentRequirement;
    private int parentRequirementCount;
    private int id;

    public GuiEditMenuParentCount(GuiBase gui, EntityPlayer player, Quest quest) {
        super(gui, player, true, 25, 20, 25, 105);

        this.id = quest.getId();
        this.useModifiedParentRequirement = quest.getUseModifiedParentRequirement();
        if (useModifiedParentRequirement) {
            this.parentRequirementCount = quest.getParentRequirementCount();
        } else {
            this.parentRequirementCount = quest.getRequirement().size();
        }


        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.parentCount.count") {
            @Override
            protected void setValue(int number) {
                parentRequirementCount = number;
            }

            @Override
            protected int getValue() {
                return parentRequirementCount;
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
    protected void save(GuiBase gui) {
        Quest quest = Quest.getQuest(id);
        if (quest != null) {
            quest.setUseModifiedParentRequirement(useModifiedParentRequirement);
            quest.setParentRequirementCount(parentRequirementCount);
            SaveHelper.add(SaveHelper.EditType.PARENT_REQUIREMENT_CHANGED);
        }
    }
}
