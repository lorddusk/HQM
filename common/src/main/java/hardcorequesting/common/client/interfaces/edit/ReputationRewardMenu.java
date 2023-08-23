package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReputationRewardMenu extends GuiEditMenu {
    
    private static final int START_X = 20;
    private static final int START_Y = 50;
    private static final int ERROR_Y = 20;
    private static final int OFFSET = 15;
    
    private final Consumer<List<ReputationReward>> resultConsumer;
    private final List<ReputationReward> rewards;
    private ReputationReward selectedReward;
    private List<FormattedText> error;
    private final NumberTextBox valueTextBox;
    
    public static void display(GuiQuestBook gui, List<ReputationReward> rewards, Consumer<List<ReputationReward>> resultConsumer) {
        gui.setEditMenu(new ReputationRewardMenu(gui, rewards, resultConsumer));
    }
    
    private ReputationRewardMenu(GuiQuestBook gui, List<ReputationReward> rewards, Consumer<List<ReputationReward>> resultConsumer) {
        super(gui, true);
        this.resultConsumer = resultConsumer;
    
        this.rewards = new ArrayList<>();
        if (rewards != null) {
            for (ReputationReward reward : rewards) {
                this.rewards.add(new ReputationReward(reward.getReward(), reward.getValue()));
            }
        }
        
        addTextBox(valueTextBox = new NumberTextBox(gui, 185, 55, Translator.translatable("hqm.repReward.value"), true,
                () -> selectedReward.getValue(), value -> selectedReward.setValue(value)) {
            @Override
            protected boolean isVisible() {
                return selectedReward != null;
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.repReward.create", 20, 20) {
            @Override
            public boolean isVisible() {
                return isValid();
            }
            
            @Override
            public void onClick() {
                ReputationRewardMenu.this.rewards.add(new ReputationReward(ReputationManager.getInstance().getReputationList().get(0), 0));
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.repReward.delete", 80, 20) {
            @Override
            public boolean isVisible() {
                return isValid() && selectedReward != null;
            }
            
            @Override
            public void onClick() {
                ReputationRewardMenu.this.rewards.remove(selectedReward);
                selectedReward = null;
            }
        });
        
        addClickable(new ArrowSelectionHelper(gui, 185, 25) {
    
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
            protected FormattedText getArrowText() {
                return selectedReward.getReward().getName();
            }
        });
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        super.draw(graphics, mX, mY);
        
        if (isValid()) {
            for (int i = 0; i < rewards.size(); i++) {
                FormattedText str = rewards.get(i).getLabel();
                boolean hover = gui.inBounds(START_X, START_Y + i * OFFSET, gui.getStringWidth(str), 9, mX, mY);
                boolean selected = rewards.get(i).equals(selectedReward);
                gui.drawString(graphics, str, START_X, START_Y + i * OFFSET, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
        } else {
            if (error == null) {
                error = gui.getLinesFromText(Translator.translatable("hqm.repReward.noValidReps"), 0.7F, 140);
            }
            
            gui.drawString(graphics, error, START_X, ERROR_Y, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        if (isValid()) {
            for (int i = 0; i < rewards.size(); i++) {
                if (gui.inBounds(START_X, START_Y + i * OFFSET, gui.getStringWidth(rewards.get(i).getLabel()), 9, mX, mY)) {
                    if (rewards.get(i).equals(selectedReward)) {
                        selectedReward = null;
                    } else {
                        selectedReward = rewards.get(i);
                        valueTextBox.reloadText();
                    }
                    break;
                }
            }
        }
    }
    
    private boolean isValid() {
        return !ReputationManager.getInstance().getReputationList().isEmpty();
    }
    
    @Override
    public void save() {
        resultConsumer.accept(rewards);
    }
}
