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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiEditMenuReputationSetting extends GuiEditMenu {
    
    private static final int BARS_X = 20;
    private static final int LOWER_Y = 50;
    private static final int UPPER_Y = 90;
    private static final int RESULT_Y = 150;
    private static final int BAR_OFFSET_Y = 10;
    private Reputation reputation;
    private int reputationId;
    private ReputationMarker lower;
    private ReputationMarker upper;
    private boolean inverted;
    private ReputationTask<?> task;
    private int id;
    private final ArrowSelectionHelper selectionHelper;
    
    public GuiEditMenuReputationSetting(GuiQuestBook gui, UUID playerId, ReputationTask<?> task, int id, ReputationTask.Part setting) {
        super(gui, playerId, true);
    
        this.task = task;
        this.id = id;
        ReputationManager reputationManager = ReputationManager.getInstance();
        if (setting.getReputation() == null) {
            if (!reputationManager.getReputations().isEmpty()) {
                reputation = reputationManager.getReputationList().get(0);
                reputationId = 0;
            } else {
                reputationId = -1;
            }
        } else {
            reputation = setting.getReputation();
            id = -1;
            List<Reputation> reputationList = new ArrayList<>(reputationManager.getReputations().values());
            for (int i = 0; i < reputationList.size(); i++) {
                Reputation element = reputationList.get(i);
                if (element.equals(reputation)) {
                    id = i;
                    break;
                }
            }
            if (id == -1) {
                reputation = null;
            } else {
                lower = setting.getLower();
                upper = setting.getUpper();
                inverted = setting.isInverted();
            }
        }
        
        addCheckBox(new AbstractCheckBox(gui, Translator.translatable("hqm.repSetting.invRange"), 21, 124) {
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
        
        selectionHelper = new ArrowSelectionHelper(gui, 25, 25) {
            @Override
            protected void onArrowClick(boolean left) {
                if (reputation != null) {
                    ReputationManager reputationManager = ReputationManager.getInstance();
                    reputationId += left ? -1 : 1;
                    if (reputationId < 0) {
                        reputationId = reputationManager.getReputations().size() - 1;
                    } else if (reputationId >= reputationManager.getReputations().size()) {
                        reputationId = 0;
                    }
                    lower = null;
                    upper = null;
                    reputation = reputationManager.getReputationList().get(reputationId);
                }
            }
    
            @Override
            protected FormattedText getArrowText() {
                if (ReputationManager.getInstance().getReputations().isEmpty()) {
                    return Translator.translatable("hqm.repSetting.invalid");
                } else {
                    return reputation != null ? Translator.plain(reputation.getName()) : Translator.translatable("hqm.repSetting.invalid");
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
        };
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        if (reputation != null) {
            
            
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.lower"), BARS_X, LOWER_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            String info = reputation.drawAndGetTooltip(matrices, gui, BARS_X, LOWER_Y + BAR_OFFSET_Y, mX, mY, null, playerId, false, null, null, false, lower, lower == null ? FormattedText.EMPTY : Translator.plain("Selected: " + lower.getLabel()), false);
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.upper"), BARS_X, UPPER_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.drawAndGetTooltip(matrices, gui, BARS_X, UPPER_Y + BAR_OFFSET_Y, mX, mY, info, playerId, false, null, null, false, upper, upper == null ? FormattedText.EMPTY : Translator.plain("Selected: " + upper.getLabel()), false);
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.preview"), BARS_X, RESULT_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.drawAndGetTooltip(matrices, gui, BARS_X, RESULT_Y + BAR_OFFSET_Y, mX, mY, info, playerId, true, lower, upper, inverted, null, null, false);
            
            
            if (info != null) {
                gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
            }
        }
        
        selectionHelper.render(matrices, mX, mY);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        if (reputation != null) {
            ReputationMarker marker = reputation.onActiveClick((GuiQuestBook) gui, BARS_X, LOWER_Y + BAR_OFFSET_Y, mX, mY);
            if (marker != null) {
                if (marker.equals(lower)) {
                    lower = null;
                } else {
                    lower = marker;
                }
            } else {
                marker = reputation.onActiveClick((GuiQuestBook) gui, BARS_X, UPPER_Y + BAR_OFFSET_Y, mX, mY);
                if (marker != null) {
                    if (marker.equals(upper)) {
                        upper = null;
                    } else {
                        upper = marker;
                    }
                }
            }
        }
        
        selectionHelper.onClick(mX, mY);
    }
    
    @Override
    public void onRelease(int mX, int mY, int button) {
        super.onRelease(mX, mY, button);
        
        selectionHelper.onRelease(mX, mY);
    }
    
    @Override
    public void save() {
        if (reputation != null) {
            task.setSetting(id, new ReputationTask.Part(reputation, lower, upper, inverted));
        }
    }
}
