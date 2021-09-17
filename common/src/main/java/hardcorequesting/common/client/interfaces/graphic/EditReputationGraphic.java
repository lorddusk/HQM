package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationValue;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static hardcorequesting.common.client.interfaces.GuiQuestBook.selectedReputation;

@Environment(EnvType.CLIENT)
public class EditReputationGraphic extends Graphic {
    public static final int REPUTATION_LIST_X = 20;
    public static final int REPUTATION_MARKER_LIST_X = 180;
    public static final int REPUTATION_LIST_Y = 20;
    public static final int REPUTATION_MARKER_LIST_Y = 35;
    public static final int REPUTATION_NEUTRAL_Y = 20;
    public static final int REPUTATION_OFFSET = 20;
    public static final int FONT_HEIGHT = 9;
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        ReputationManager reputationManager = ReputationManager.getInstance();
        Map<String, Reputation> reputationMap = reputationManager.getReputations();
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationMap.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationMap.size());
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (int i = start; i < end; i++) {
                int x = REPUTATION_LIST_X;
                int y = REPUTATION_LIST_Y + (i - start) * REPUTATION_OFFSET;
                String str = reputationList.get(i).getName();
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY);
                boolean selected = reputationList.get(i).equals(selectedReputation);
                
                gui.drawString(matrices, Translator.plain(str), x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
        }
        
        if (selectedReputation != null) {
            FormattedText neutralName = Translator.translatable("hqm.rep.neutral", selectedReputation.getNeutralName());
            gui.drawString(matrices, neutralName, REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY) ? 0xAAAAAA : 0x404040);
            
            int start = gui.reputationTierScroll.isVisible(gui) ? Math.round((selectedReputation.getMarkerCount() - GuiQuestBook.VISIBLE_REPUTATION_TIERS) * gui.reputationTierScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATION_TIERS, selectedReputation.getMarkerCount());
            for (int i = start; i < end; i++) {
                int x = REPUTATION_MARKER_LIST_X;
                int y = REPUTATION_MARKER_LIST_Y + (i - start) * REPUTATION_OFFSET;
                String str = selectedReputation.getMarker(i).getTitle();
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY);
                gui.drawString(matrices, Translator.plain(str), x, y, hover ? 0xAAAAAA : 0x404040);
            }
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        UUID playerId = gui.getPlayer().getUUID();
        
        ReputationManager reputationManager = ReputationManager.getInstance();
        Map<String, Reputation> reputationMap = reputationManager.getReputations();
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationMap.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationMap.size());
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (int i = start; i < end; i++) {
                int x = REPUTATION_LIST_X;
                int y = REPUTATION_LIST_Y + (i - start) * REPUTATION_OFFSET;
                Reputation reputation = reputationList.get(i);
                String str = reputation.getName();
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.NORMAL) {
                        if (reputation.equals(selectedReputation)) {
                            selectedReputation = null;
                        } else {
                            selectedReputation = reputation;
                        }
                    } else if (gui.getCurrentMode() == EditMode.RENAME) {
                        TextMenu.display(gui, playerId, reputation.getName(), true, reputation::setName);
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        if (selectedReputation == reputation) {
                            selectedReputation = null;
                        }
                        
                        for (Quest quest : Quest.getQuests().values()) {
                            for (QuestTask<?> task : quest.getTasks()) {
                                if (task instanceof ReputationTask) {
                                    ReputationTask<?> reputationTask = (ReputationTask<?>) task;
                                    List<ReputationTask.Part> settings = reputationTask.getSettings();
                                    settings.removeIf(setting -> reputation.equals(setting.getReputation()));
                                }
                            }
                            
                            List<ReputationReward> rewards = quest.getRewards().getReputationRewards();
                            if (rewards != null) {
                                rewards.removeIf(reward -> reputation.equals(reward.getReward()));
                            }
                            
                        }
                        
                        reputationMap.remove(reputation.getId());
                        SaveHelper.add(EditType.REPUTATION_REMOVE);
                    }
                    return;
                }
                
            }
        }
        
        if (selectedReputation != null) {
            FormattedText neutralName = Translator.translatable("hqm.rep.neutral", selectedReputation.getNeutralName());
            if (gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.RENAME) {
                    TextMenu.display(gui, playerId, selectedReputation.getNeutralName(), true, selectedReputation::setNeutralName);
                }
                return;
            }
            
            int start = gui.reputationTierScroll.isVisible(gui) ? Math.round((selectedReputation.getMarkerCount() - GuiQuestBook.VISIBLE_REPUTATION_TIERS) * gui.reputationTierScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATION_TIERS, selectedReputation.getMarkerCount());
            for (int i = start; i < end; i++) {
                int x = REPUTATION_MARKER_LIST_X;
                int y = REPUTATION_MARKER_LIST_Y + (i - start) * REPUTATION_OFFSET;
                String str = selectedReputation.getMarker(i).getTitle();
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.RENAME) {
                        ReputationMarker marker = selectedReputation.getMarker(i);
                        TextMenu.display(gui, playerId, marker.getName(), true, marker::setName);
                    } else if (gui.getCurrentMode() == EditMode.REPUTATION_VALUE) {
                        gui.setEditMenu(new GuiEditMenuReputationValue(gui, playerId, selectedReputation.getMarker(i)));
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        for (Quest quest : Quest.getQuests().values()) {
                            for (QuestTask<?> task : quest.getTasks()) {
                                if (task instanceof ReputationTask<?>) {
                                    ReputationTask<?> reputationTask = (ReputationTask<?>) task;
                                    for (ReputationTask.Part setting : reputationTask.getSettings()) {
                                        if (selectedReputation.getMarker(i).equals(setting.getLower())) {
                                            setting.setLower(null);
                                        }
                                        if (selectedReputation.getMarker(i).equals(setting.getUpper())) {
                                            setting.setUpper(null);
                                        }
                                    }
                                }
                            }
                        }
                        
                        selectedReputation.remove(i);
                        SaveHelper.add(EditType.REPUTATION_MARKER_REMOVE);
                    }
                    
                    return;
                }
            }
        }
    }
}
