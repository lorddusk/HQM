package hardcorequesting.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.quests.task.QuestTaskReputation;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationMarker;
import hardcorequesting.util.Translator;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GuiEditMenuReputationSetting extends GuiEditMenuExtended {
    
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
    private QuestTaskReputation task;
    private int id;
    
    public GuiEditMenuReputationSetting(GuiQuestBook gui, Player player, QuestTaskReputation task, int id, QuestTaskReputation.ReputationSetting setting) {
        super(gui, player, true, 25, 25, -1, -1);
        
        this.task = task;
        this.id = id;
        if (setting == null || setting.getReputation() == null) {
            if (!Reputation.getReputations().isEmpty()) {
                reputation = Reputation.getReputationList().get(0);
                reputationId = 0;
            } else {
                reputationId = -1;
            }
        } else {
            reputation = setting.getReputation();
            id = -1;
            List<Reputation> reputationList = new ArrayList<>(Reputation.getReputations().values());
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
        
        checkboxes.add(new CheckBox("hqm.repSetting.invRange", 21, 124) {
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
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        if (reputation != null) {
            
            String info = null;
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.lower"), BARS_X, LOWER_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.draw(matrices, (GuiQuestBook) gui, BARS_X, LOWER_Y + BAR_OFFSET_Y, mX, mY, info, player, false, null, null, false, lower, lower == null ? "" : "Selected: " + lower.getLabel(), false);
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.upper"), BARS_X, UPPER_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.draw(matrices, (GuiQuestBook) gui, BARS_X, UPPER_Y + BAR_OFFSET_Y, mX, mY, info, player, false, null, null, false, upper, upper == null ? "" : "Selected: " + upper.getLabel(), false);
            
            gui.drawString(matrices, Translator.translatable("hqm.repSetting.preview"), BARS_X, RESULT_Y, 0x404040);
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.draw(matrices, (GuiQuestBook) gui, BARS_X, RESULT_Y + BAR_OFFSET_Y, mX, mY, info, player, true, lower, upper, inverted, null, null, false);
            
            
            if (info != null) {
                gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
            }
        }
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
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
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        if (reputation != null) {
            reputationId += left ? -1 : 1;
            if (reputationId < 0) {
                reputationId = Reputation.getReputations().size() - 1;
            } else if (reputationId >= Reputation.getReputations().size()) {
                reputationId = 0;
            }
            lower = null;
            upper = null;
            reputation = Reputation.getReputationList().get(reputationId);
        }
    }
    
    @Override
    protected String getArrowText() {
        if (Reputation.getReputations().isEmpty()) {
            return I18n.get("hqm.repSetting.invalid");
        } else {
            return reputation != null ? reputation.getName() : I18n.get("hqm.repSetting.invalid");
        }
    }
    
    @Override
    protected String getArrowDescription() {
        if (Reputation.getReputations().isEmpty()) {
            return I18n.get("hqm.repReward.noValidReps");
        } else {
            return null;
        }
    }
    
    @Override
    public void save(GuiBase gui) {
        if (reputation != null) {
            task.setSetting(id, new QuestTaskReputation.ReputationSetting(reputation, lower, upper, inverted));
        }
    }
}
