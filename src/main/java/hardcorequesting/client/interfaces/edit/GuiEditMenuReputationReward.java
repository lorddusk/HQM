package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.LargeButton;
import hardcorequesting.quests.reward.ReputationReward;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class GuiEditMenuReputationReward extends GuiEditMenuExtended {

    private List<ReputationReward> rewards;
    private ReputationReward selectedReward;

    public GuiEditMenuReputationReward(GuiBase gui, EntityPlayer player, List<ReputationReward> rewards) {
        super(gui, player, true, 185, 25, 185, 55);

        this.rewards = new ArrayList<>();
        if (rewards != null) {
            for (ReputationReward reward : rewards) {
                this.rewards.add(new ReputationReward(reward.getReward(), reward.getValue()));
            }
        }

        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.repReward.value") {
            @Override
            protected int getValue() {
                return selectedReward.getValue();
            }

            @Override
            protected void setValue(int number) {
                selectedReward.setValue(number);
            }

            @Override
            protected boolean isVisible() {
                return selectedReward != null;
            }

            @Override
            protected boolean isNegativeAllowed() {
                return true;
            }
        });

        buttons.add(new LargeButton("hqm.repReward.create", 20, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return isValid();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                GuiEditMenuReputationReward.this.rewards.add(new ReputationReward(Reputation.getReputations().get(0), 0));
            }
        });

        buttons.add(new LargeButton("hqm.repReward.delete", 80, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return isValid() && selectedReward != null;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                GuiEditMenuReputationReward.this.rewards.remove(selectedReward);
                selectedReward = null;
            }
        });
    }

    private static final int START_X = 20;
    private static final int START_Y = 50;
    private static final int ERROR_Y = 20;
    private static final int OFFSET = 15;

    private List<String> error;

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        if (isValid()) {
            for (int i = 0; i < rewards.size(); i++) {
                String str = rewards.get(i).getLabel();
                boolean hover = gui.inBounds(START_X, START_Y + i * OFFSET, gui.getStringWidth(str), 9, mX, mY);
                boolean selected = rewards.get(i).equals(selectedReward);
                gui.drawString(str, START_X, START_Y + i * OFFSET, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
        } else {
            if (error == null) {
                error = gui.getLinesFromText(Translator.translate("hqm.repReward.noValidReps"), 0.7F, 140);
            }

            gui.drawString(error, START_X, ERROR_Y, 0.7F, 0x404040);
        }
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);

        if (isValid()) {
            for (int i = 0; i < rewards.size(); i++) {
                if (gui.inBounds(START_X, START_Y + i * OFFSET, gui.getStringWidth(rewards.get(i).getLabel()), 9, mX, mY)) {
                    if (rewards.get(i).equals(selectedReward)) {
                        selectedReward = null;
                    } else {
                        selectedReward = rewards.get(i);
                        textBoxes.getTextBoxes().get(0).reloadText(gui);
                    }
                    break;
                }
            }
        }
    }

    private boolean isValid() {
        return !Reputation.getReputations().isEmpty();
    }

    @Override
    protected boolean isArrowVisible() {
        return isValid() && selectedReward != null;
    }

    @Override
    protected void onArrowClick(boolean left) {
        if (selectedReward != null && selectedReward.getReward() != null) {
            for (int i = 0; i < Reputation.getReputations().size(); i++) {
                if (Reputation.getReputations().get(i).equals(selectedReward.getReward())) {
                    int id = i + (left ? -1 : 1);
                    if (id < 0) {
                        id = Reputation.getReputations().size() - 1;
                    } else if (id >= Reputation.getReputations().size()) {
                        id = 0;
                    }
                    selectedReward.setReward(Reputation.getReputations().get(id));
                    break;
                }
            }
        }
    }

    @Override
    protected String getArrowText() {
        return selectedReward.getReward().getName();
    }

    @Override
    protected String getArrowDescription() {
        return null;
    }

    @Override
    public void save(GuiBase gui) {
        GuiQuestBook.selectedQuest.setReputationRewards(rewards.isEmpty() ? null : rewards);
        SaveHelper.add(SaveHelper.EditType.REPUTATION_REWARD_CHANGE);
    }
}
