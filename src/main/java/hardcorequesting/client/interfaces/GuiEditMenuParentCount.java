package hardcorequesting.client.interfaces;


import hardcorequesting.SaveHelper;
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
        }else{
            this.parentRequirementCount = quest.getRequirement().size();
        }


        textBoxes.add(new TextBoxNumber(gui, 0, "Parent count") {
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
        return useModifiedParentRequirement ? "Requires specified amount" : "Requires all";
    }

    @Override
    protected String getArrowDescription() {
        return useModifiedParentRequirement ? "For this quest to unlock the player will have to complete a certain amount of parent quests. The required amount can be specified below." : "All parent quests have to be completed before this quest unlocks.";
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
