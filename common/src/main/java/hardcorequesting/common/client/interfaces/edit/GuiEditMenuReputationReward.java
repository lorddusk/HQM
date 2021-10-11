package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class GuiEditMenuReputationReward extends GuiEditMenu {
    
    private static final int START_X = 20;
    private static final int START_Y = 50;
    private static final int ERROR_Y = 20;
    private static final int OFFSET = 15;
    
    private final Consumer<List<ReputationReward>> resultConsumer;
    private final List<ReputationReward> rewards;
    private ReputationReward selectedReward;
    private List<FormattedText> error;
    private final NumberTextBox valueTextBox;
    private final ArrowSelectionHelper selectionHelper;
    
    public GuiEditMenuReputationReward(GuiQuestBook gui, UUID playerId, List<ReputationReward> rewards, Consumer<List<ReputationReward>> resultConsumer) {
        super(gui, playerId, true);
        this.resultConsumer = resultConsumer;
    
        this.rewards = new ArrayList<>();
        if (rewards != null) {
            for (ReputationReward reward : rewards) {
                this.rewards.add(new ReputationReward(reward.getReward(), reward.getValue()));
            }
        }
        
        addTextBox(valueTextBox = new NumberTextBox(gui, 185, 55, "hqm.repReward.value") {
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
        
        addButton(new LargeButton(gui, "hqm.repReward.create", 20, 20) {
            @Override
            public boolean isVisible() {
                return isValid();
            }
            
            @Override
            public void onClick() {
                GuiEditMenuReputationReward.this.rewards.add(new ReputationReward(ReputationManager.getInstance().getReputationList().get(0), 0));
            }
        });
        
        addButton(new LargeButton(gui, "hqm.repReward.delete", 80, 20) {
            @Override
            public boolean isVisible() {
                return isValid() && selectedReward != null;
            }
            
            @Override
            public void onClick() {
                GuiEditMenuReputationReward.this.rewards.remove(selectedReward);
                selectedReward = null;
            }
        });
        
        selectionHelper = new ArrowSelectionHelper(gui, 185, 25) {
    
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
                return Translator.plain(selectedReward.getReward().getName());
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return null;
            }
    
        };
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
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
        
        selectionHelper.render(matrices, mX, mY);
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
        
        selectionHelper.onClick(mX, mY);
    }
    
    @Override
    public void onRelease(int mX, int mY, int button) {
        super.onRelease(mX, mY, button);
        
        selectionHelper.onRelease();
    }
    
    private boolean isValid() {
        return !ReputationManager.getInstance().getReputationList().isEmpty();
    }
    
    @Override
    public void save() {
        resultConsumer.accept(rewards);
    }
}
