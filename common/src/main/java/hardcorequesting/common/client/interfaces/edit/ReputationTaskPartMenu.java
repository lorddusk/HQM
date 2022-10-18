package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ReputationTaskPartMenu extends GuiEditMenu {
    
    private static final int BARS_X = 20;
    private static final int LOWER_Y = 50;
    private static final int UPPER_Y = 90;
    private static final int RESULT_Y = 150;
    private static final int BAR_OFFSET_Y = 10;
    
    private final Consumer<ReputationTask.Part> resultConsumer;
    private final UUID playerId;
    private Reputation reputation = null;
    private int reputationId = -1;
    private ReputationMarker lower;
    private ReputationMarker upper;
    private boolean inverted;
    
    public static void display(GuiQuestBook gui, UUID playerId, ReputationTask.Part setting, Consumer<ReputationTask.Part> resultConsumer) {
        gui.setEditMenu(new ReputationTaskPartMenu(gui, playerId, setting, resultConsumer));
    }
    
    private ReputationTaskPartMenu(GuiQuestBook gui, UUID playerId, ReputationTask.Part setting, Consumer<ReputationTask.Part> resultConsumer) {
        super(gui, true);
    
        this.resultConsumer = resultConsumer;
        this.playerId = playerId;
        ReputationManager reputationManager = ReputationManager.getInstance();
        if (setting.getReputation() == null) {
            setReputation(0);
        } else {
            int id = reputationManager.getReputationList().indexOf(reputation);
            if (reputationId != -1) {
                setReputation(id);
                lower = setting.getLower();
                upper = setting.getUpper();
            } else setReputation(0);
            inverted = setting.isInverted();
        }
        
        addClickable(new AbstractCheckBox(gui, Translator.translatable("hqm.repSetting.invRange"), 21, 124) {
            @Override
            protected boolean isVisible() {
                return reputation != null;
            }
            
            @Override
            public boolean getValue() {
                return inverted;
            }
            
            @Override
            public void setValue(boolean val) {
                inverted = val;
            }
        });
        
        addClickable(new ArrowSelectionHelper(gui, 25, 25) {
            @Override
            protected void onArrowClick(boolean left) {
                if (reputationId != -1) {
                    setReputation(reputationId + (left ? -1 : 1));
                }
            }
    
            @Override
            protected FormattedText getArrowText() {
                if (ReputationManager.getInstance().getReputations().isEmpty()) {
                    return Translator.translatable("hqm.repSetting.invalid");
                } else {
                    return reputation != null ? reputation.getName() : Translator.translatable("hqm.repSetting.invalid");
                }
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                if (ReputationManager.getInstance().getReputations().isEmpty()) {
                    return Translator.translatable("hqm.repReward.noValidReps");
                } else {
                    return null;
                }
            }
        });
    }
    
    private void setReputation(int reputationId) {
        List<Reputation> reputations = ReputationManager.getInstance().getReputationList();
        if (!reputations.isEmpty()) {
            if (reputationId < 0)
                this.reputationId = reputations.size() - 1;
            else if (reputationId >= reputations.size())
                this.reputationId = 0;
            else this.reputationId = reputationId;
            reputation = reputations.get(this.reputationId);
            lower = null;
            upper = null;
        }
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        if (reputation != null) {
            
            
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.lower"), BARS_X, LOWER_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            FormattedText info = reputation.drawAndGetTooltip(matrices, gui, BARS_X, LOWER_Y + BAR_OFFSET_Y, mX, mY, null, playerId, false, null, null, false, lower, lower == null ? FormattedText.EMPTY : Translator.text("Selected: ").append(lower.getLabel()), false);
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.upper"), BARS_X, UPPER_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.drawAndGetTooltip(matrices, gui, BARS_X, UPPER_Y + BAR_OFFSET_Y, mX, mY, info, playerId, false, null, null, false, upper, upper == null ? FormattedText.EMPTY : Translator.text("Selected: ").append(upper.getLabel()), false);
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.preview"), BARS_X, RESULT_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.drawAndGetTooltip(matrices, gui, BARS_X, RESULT_Y + BAR_OFFSET_Y, mX, mY, info, playerId, true, lower, upper, inverted, null, null, false);
            
            
            if (info != null) {
                gui.renderTooltip(matrices, info, mX + gui.getLeft(), mY + gui.getTop());
            }
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        if (reputation != null) {
            ReputationMarker marker = reputation.onActiveClick(gui, BARS_X, LOWER_Y + BAR_OFFSET_Y, mX, mY);
            if (marker != null) {
                if (marker.equals(lower)) {
                    lower = null;
                } else {
                    lower = marker;
                }
            } else {
                marker = reputation.onActiveClick(gui, BARS_X, UPPER_Y + BAR_OFFSET_Y, mX, mY);
                if (marker != null) {
                    if (marker.equals(upper)) {
                        upper = null;
                    } else {
                        upper = marker;
                    }
                }
            }
        }
    }
    
    @Override
    public void save() {
        if (reputation != null) {
            resultConsumer.accept(new ReputationTask.Part(reputation, lower, upper, inverted));
        }
    }
}
