package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GuiEditMenuReputationReward extends GuiEditMenuExtended {
    
    private static final int START_X = 20;
    private static final int START_Y = 50;
    private static final int ERROR_Y = 20;
    private static final int OFFSET = 15;
    private List<ReputationReward> rewards;
    private ReputationReward selectedReward;
    private List<FormattedText> error;
    
    public GuiEditMenuReputationReward(GuiBase gui, Player player, List<ReputationReward> rewards) {
        super(gui, player, true, 185, 25);
        
        this.rewards = new ArrayList<>();
        if (rewards != null) {
            for (ReputationReward reward : rewards) {
                this.rewards.add(new ReputationReward(reward.getReward(), reward.getValue()));
            }
        }
        
        textBoxes.add(new TextBoxNumber(gui, 185, 55, "hqm.repReward.value") {
            @Override
            protected boolean isVisible() {
                return selectedReward != null;
            }
            
            @Override
            protected boolean isNegativeAllowed() {
                return true;
            }
            
            @Override
            protected int getValue() {
                return selectedReward.getValue();
            }
            
            @Override
            protected void setValue(int number) {
                selectedReward.setValue(number);
            }
        });
        
        buttons.add(new LargeButton("hqm.repReward.create", 20, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return isValid();
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                GuiEditMenuReputationReward.this.rewards.add(new ReputationReward(ReputationManager.getInstance().getReputationList().get(0), 0));
            }
        });
        
        buttons.add(new LargeButton("hqm.repReward.delete", 80, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return isValid() && selectedReward != null;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                GuiEditMenuReputationReward.this.rewards.remove(selectedReward);
                selectedReward = null;
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        if (isValid()) {
            for (int i = 0; i < rewards.size(); i++) {
                FormattedText str = Translator.plain(rewards.get(i).getLabel());
                boolean hover = gui.inBounds(START_X, START_Y + i * OFFSET, gui.getStringWidth(str), 9, mX, mY);
                boolean selected = rewards.get(i).equals(selectedReward);
                gui.drawString(matrices, str, START_X, START_Y + i * OFFSET, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
        } else {
            if (error == null) {
                error = gui.getLinesFromText(Translator.translatable("hqm.repReward.noValidReps"), 0.7F, 140);
            }
            
            gui.drawString(matrices, error, START_X, ERROR_Y, 0.7F, 0x404040);
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
    
    @Override
    protected boolean isArrowVisible() {
        return isValid() && selectedReward != null;
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        if (selectedReward != null && selectedReward.getReward() != null) {
            ReputationManager reputationManager = ReputationManager.getInstance();
            for (int i = 0; i < reputationManager.getReputationList().size(); i++) {
                if (reputationManager.getReputationList().get(i).equals(selectedReward.getReward())) {
                    int id = i + (left ? -1 : 1);
                    if (id < 0) {
                        id = reputationManager.getReputationList().size() - 1;
                    } else if (id >= reputationManager.getReputationList().size()) {
                        id = 0;
                    }
                    selectedReward.setReward(reputationManager.getReputationList().get(id));
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
    
    private boolean isValid() {
        return !ReputationManager.getInstance().getReputationList().isEmpty();
    }
    
    @Override
    public void save(GuiBase gui) {
        GuiQuestBook.selectedQuest.getRewards().setReputationRewards(rewards.isEmpty() ? null : rewards);
        SaveHelper.add(EditType.REPUTATION_REWARD_CHANGE);
    }
}
