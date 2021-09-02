package hardcorequesting.common.reputation;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationValue;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.*;

import static hardcorequesting.common.client.interfaces.GuiQuestBook.selectedReputation;

public class Reputation {
    public static final int BAR_SRC_X = 0;
    public static final int BAR_SRC_Y = 101;
    public static final int BAR_WIDTH = 125;
    public static final int BAR_HEIGHT = 3;
    public static final int BAR_X = 0;
    public static final int BAR_Y = 5;
    public static final int REPUTATION_LIST_X = 20;
    public static final int REPUTATION_MARKER_LIST_X = 180;
    public static final int REPUTATION_LIST_Y = 20;
    public static final int REPUTATION_MARKER_LIST_Y = 35;
    public static final int REPUTATION_NEUTRAL_Y = 20;
    public static final int REPUTATION_OFFSET = 20;
    public static final int FONT_HEIGHT = 9;
    private static final int OFFSET_Y = 24;
    private static final int ARROW_SRC_POINTER_X = 0;
    private static final int ARROW_SRC_MARKER_X = 10;
    private static final int ARROW_SRC_NEUTRAL_X = 20;
    private static final int ARROW_MARKER_OFFSET = 5;
    private static final int ARROW_MARKER_Y = 1;
    private static final int ARROW_POINTER_Y = -5;
    private static final int ARROW_SRC_Y = 93;
    private static final int ARROW_SIZE = 5;
    private static final int TEXT_X = 5;
    private static final int TEXT_Y = 14;
    private String uuid;
    private String name;
    private ReputationMarker neutral;
    private List<ReputationMarker> markers;
    
    public Reputation(String name, String neutralName) {
        Map<String, Reputation> reputationMap = ReputationManager.getInstance().reputationMap;
        do {
            this.uuid = UUID.randomUUID().toString();
        } while (reputationMap.containsKey(this.uuid));
        this.name = name;
        this.neutral = new ReputationMarker(neutralName, 0, true);
        this.markers = new ArrayList<>();
    }
    
    public Reputation(String id, String name, String neutralName) {
        Map<String, Reputation> reputationMap = ReputationManager.getInstance().reputationMap;
        this.uuid = id;
        while (this.uuid == null || reputationMap.containsKey(this.uuid)) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.name = name;
        this.neutral = new ReputationMarker(neutralName, 0, true);
        this.markers = new ArrayList<>();
    }
    
    @Environment(EnvType.CLIENT)
    public static void drawAll(PoseStack matrices, GuiQuestBook gui, int x, int y, int mX, int mY, final Player player) {
        String info = null;
        
        List<Reputation> reputations = ReputationManager.getInstance().getReputationList();
        
        reputations.sort((reputation1, reputation2) -> Integer.compare(Math.abs(reputation2.getValue(player)), Math.abs(reputation1.getValue(player))));
        
        int start = gui.reputationDisplayScroll.isVisible(gui) ? Math.round((reputations.size() - GuiQuestBook.VISIBLE_DISPLAY_REPUTATIONS) * gui.reputationDisplayScroll.getScroll()) : 0;
        int end = Math.min(start + GuiQuestBook.VISIBLE_DISPLAY_REPUTATIONS, reputations.size());
        for (int i = start; i < end; i++) {
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputations.get(i).draw(matrices, gui, x, y + (i - start) * OFFSET_Y, mX, mY, info, player, false, null, null, false, null, null, false);
        }
        
        if (info != null) {
            gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static void drawEditPage(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        ReputationManager reputationManager = ReputationManager.getInstance();
        Map<String, Reputation> reputationMap = reputationManager.reputationMap;
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationMap.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationMap.size());
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (int i = start; i < end; i++) {
                int x = REPUTATION_LIST_X;
                int y = REPUTATION_LIST_Y + (i - start) * REPUTATION_OFFSET;
                String str = reputationList.get(i).name;
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY);
                boolean selected = reputationList.get(i).equals(selectedReputation);
                
                gui.drawString(matrices, Translator.plain(str), x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
        }
        
        if (selectedReputation != null) {
            FormattedText neutralName = Translator.translatable("hqm.rep.neutral", selectedReputation.neutral.getName());
            gui.drawString(matrices, neutralName, REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY) ? 0xAAAAAA : 0x404040);
            
            int start = gui.reputationTierScroll.isVisible(gui) ? Math.round((selectedReputation.markers.size() - GuiQuestBook.VISIBLE_REPUTATION_TIERS) * gui.reputationTierScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATION_TIERS, selectedReputation.markers.size());
            for (int i = start; i < end; i++) {
                int x = REPUTATION_MARKER_LIST_X;
                int y = REPUTATION_MARKER_LIST_Y + (i - start) * REPUTATION_OFFSET;
                String str = selectedReputation.markers.get(i).getTitle();
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY);
                gui.drawString(matrices, Translator.plain(str), x, y, hover ? 0xAAAAAA : 0x404040);
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static void onClick(GuiQuestBook gui, int mX, int mY, Player player) {
        ReputationManager reputationManager = ReputationManager.getInstance();
        Map<String, Reputation> reputationMap = reputationManager.reputationMap;
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationMap.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationMap.size());
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (int i = start; i < end; i++) {
                int x = REPUTATION_LIST_X;
                int y = REPUTATION_LIST_Y + (i - start) * REPUTATION_OFFSET;
                Reputation reputation = reputationList.get(i);
                String str = reputation.name;
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.NORMAL) {
                        if (reputation.equals(selectedReputation)) {
                            selectedReputation = null;
                        } else {
                            selectedReputation = reputation;
                        }
                    } else if (gui.getCurrentMode() == EditMode.RENAME) {
                        TextMenu.display(gui, player, reputation.getName(), true, reputation::setName);
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
            FormattedText neutralName = Translator.translatable("hqm.rep.neutral", selectedReputation.neutral.getName());
            if (gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.RENAME) {
                    TextMenu.display(gui, player, selectedReputation.neutral.getName(), true, selectedReputation.neutral::setName);
                }
                return;
            }
            
            int start = gui.reputationTierScroll.isVisible(gui) ? Math.round((selectedReputation.markers.size() - GuiQuestBook.VISIBLE_REPUTATION_TIERS) * gui.reputationTierScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATION_TIERS, selectedReputation.markers.size());
            for (int i = start; i < end; i++) {
                int x = REPUTATION_MARKER_LIST_X;
                int y = REPUTATION_MARKER_LIST_Y + (i - start) * REPUTATION_OFFSET;
                String str = selectedReputation.markers.get(i).getTitle();
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.RENAME) {
                        ReputationMarker marker = selectedReputation.markers.get(i);
                        TextMenu.display(gui, player, marker.getName(), true, marker::setName);
                    } else if (gui.getCurrentMode() == EditMode.REPUTATION_VALUE) {
                        gui.setEditMenu(new GuiEditMenuReputationValue(gui, player, selectedReputation.markers.get(i)));
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        for (Quest quest : Quest.getQuests().values()) {
                            for (QuestTask<?> task : quest.getTasks()) {
                                if (task instanceof ReputationTask<?>) {
                                    ReputationTask<?> reputationTask = (ReputationTask<?>) task;
                                    for (ReputationTask.Part setting : reputationTask.getSettings()) {
                                        if (selectedReputation.markers.get(i).equals(setting.getLower())) {
                                            setting.setLower(null);
                                        }
                                        if (selectedReputation.markers.get(i).equals(setting.getUpper())) {
                                            setting.setUpper(null);
                                        }
                                    }
                                }
                            }
                        }
                        
                        selectedReputation.markers.remove(i);
                        selectedReputation.sort();
                        SaveHelper.add(EditType.REPUTATION_MARKER_REMOVE);
                    }
                    
                    return;
                }
            }
        }
    }
    
    public String getId() {
        return uuid;
    }
    
    public String getNeutralName() {
        return neutral.getName();
    }
    
    public void sort() {
        Collections.sort(markers);
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).setId(i);
        }
        neutral.setId(markers.size());
    }
    
    public int getValue(Player player) {
        return getValue(player.getUUID());
    }
    
    public int getValue(UUID playerID) {
        return QuestingDataManager.getInstance().getQuestingData(playerID).getTeam().getReputation(this);
    }
    
    @Environment(EnvType.CLIENT)
    public String draw(PoseStack matrices, GuiQuestBook gui, int x, int y, int mX, int mY, String info, Player player, boolean effects, ReputationMarker lower, ReputationMarker upper, boolean inverted, ReputationMarker active, String text, boolean completed) {
        String error = getError();
        
        if (error != null) {
            gui.drawRect(matrices, x + BAR_X, y + BAR_Y, BAR_SRC_X, BAR_SRC_Y, BAR_WIDTH, BAR_HEIGHT);
            gui.drawString(matrices, Translator.plain(error), x + TEXT_X, y + TEXT_Y, 0.7F, GuiColor.RED.getHexColor());
            return info;
        }
        
        int lowerValue = 0;
        int upperValue = 0;
        boolean lowerMoved = false;
        boolean upperMoved = false;
        boolean lowerMovedInner = false;
        boolean upperMovedInner = false;
        boolean lowerOnMarker = false;
        boolean upperOnMarker = false;
        if (effects) {
            if (lower == null) {
                lowerValue = Math.min(markers.get(0).getValue(), 0);
            } else {
                lowerValue = lower.getValue();
                lowerOnMarker = lower.getId() == 0 && lower.getValue() > 0;
                if (lower.isNeutral()) {
                    lowerOnMarker = true;
                    lowerMovedInner = true;
                    lowerMoved = true;
                } else if (lower.getId() == markers.size() - 1) {
                    lowerOnMarker = true;
                } else if (lowerValue <= 0) {
                    for (int i = 0; i < markers.size(); i++) {
                        if (markers.get(i).getValue() >= lowerValue) {
                            lowerValue = markers.get(i).getValue();
                            if (i - 1 != 0) {
                                lowerMoved = true;
                            }
                            break;
                        }
                    }
                }
            }
            if (upper == null) {
                upperValue = Math.max(markers.get(markers.size() - 1).getValue(), 0);
            } else {
                upperValue = upper.getValue();
                upperOnMarker = upper.getId() == markers.size() - 1 && upper.getValue() < 0;
                if (upper.isNeutral()) {
                    upperOnMarker = true;
                    upperMovedInner = true;
                    upperMoved = true;
                } else if (upper.getId() == 0) {
                    upperOnMarker = true;
                } else if (upperValue >= 0) {
                    for (int i = markers.size() - 1; i >= 0; i--) {
                        if (markers.get(i).getValue() <= upperValue) {
                            upperValue = markers.get(i).getValue();
                            if (i + 1 == markers.size()) {
                                upperMoved = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        int normalSrcY = BAR_SRC_Y;
        int selectedSrcY = BAR_SRC_Y - BAR_HEIGHT;
        
        if (effects && inverted) {
            normalSrcY = selectedSrcY;
            selectedSrcY = BAR_SRC_Y;
        }
        
        gui.drawRect(matrices, x + BAR_X, y + BAR_Y, BAR_SRC_X, normalSrcY, BAR_WIDTH, BAR_HEIGHT);
        if (effects) {
            int leftX = getPointerPosition(lowerValue, lowerOnMarker);
            if (lowerMoved) {
                leftX += lowerMovedInner ? 1 : 1 + ARROW_MARKER_OFFSET;
            }
            int rightX = getPointerPosition(upperValue, upperOnMarker) + 1;
            if (upperMoved) {
                rightX -= upperMovedInner ? 1 : ARROW_MARKER_OFFSET;
            }
            gui.drawRect(matrices, x + BAR_X + leftX, y + BAR_Y, BAR_SRC_X + leftX, selectedSrcY, rightX - leftX, BAR_HEIGHT);
        }
        
        for (int i = 0; i < markers.size(); i++) {
            int position = i * (BAR_WIDTH - ARROW_MARKER_OFFSET * 2) / (markers.size() - 1);
            int markerX = x + BAR_X - ARROW_SIZE / 2 + position + ARROW_MARKER_OFFSET;
            
            int markerY = y + BAR_Y + ARROW_MARKER_Y;
            int srcX = ARROW_SRC_MARKER_X;
            int value = markers.get(i).getValue();
            if (info == null && gui.inBounds(markerX, markerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
                srcX += ARROW_SIZE;
                info = markers.get(i).getName() + " (" + value + ")";
            }
            
            
            boolean selected = markers.get(i).equals(active) || (effects && ((lowerValue <= value && value <= upperValue) != inverted));
            gui.drawRect(matrices, markerX, markerY, srcX, ARROW_SRC_Y + (selected ? -ARROW_SIZE : 0), ARROW_SIZE, ARROW_SIZE);
        }
        
        ReputationMarker current = null;
        int value = 0;
        if (player != null) {
            value = getValue(player);
            current = getCurrentMarker(value);
            
            
            if (drawPointer(matrices, gui, value, x, y, ARROW_POINTER_Y, ARROW_SRC_POINTER_X, mX, mY, false)) {
                info = current.getName() + " (" + value + ")";
            }
        }
        if (drawPointer(matrices, gui, 0, x, y, ARROW_MARKER_Y, ARROW_SRC_NEUTRAL_X, mX, mY, neutral.equals(active) || (effects && ((lowerValue <= 0 && 0 <= upperValue) != inverted)))) {
            info = neutral.getName();
        }
        
        String str;
        boolean selected = false;
        
        if (text != null) {
            str = text;
        } else if (current == null || lower != null || upper != null) {
            if (lower == null && upper == null) {
                str = GuiColor.RED + I18n.get("hqm.rep" + (inverted ? "no" : "any") + "ValueOf") + " " + name;
                
            } else {
                String lowerName = lower == null ? null : Screen.hasShiftDown() ? String.valueOf(lower.getValue()) : lower.getName();
                String upperName = upper == null ? null : Screen.hasShiftDown() ? String.valueOf(upper.getValue()) : upper.getName();
                
                if (lower != null && upper != null) {
                    if (lower.equals(upper)) {
                        if (inverted) {
                            str = name + " != " + lowerName;
                        } else {
                            str = name + " == " + lowerName;
                        }
                    } else {
                        if (inverted) {
                            str = I18n.get("hqm.rep.not") + " (" + lowerName + " <= " + name + " <= " + upperName + ")";
                        } else {
                            str = lowerName + " <= " + name + " <= " + upperName;
                        }
                    }
                } else if (lower != null) {
                    str = name + " " + (inverted ? "<" : ">=") + " " + lowerName;
                } else {
                    str = name + " " + (inverted ? ">" : "<=") + " " + upperName;
                }
            }
        } else {
            str = name + ": " + current.getName() + " (" + value + ")";
            selected = completed || (effects && ((lowerValue <= current.getValue() && current.getValue() <= upperValue) != inverted));
        }
        
        gui.drawString(matrices, Translator.plain(str), x + TEXT_X, y + TEXT_Y, 0.7F, selected ? 0x40AA40 : 0x404040);
        
        return info;
    }
    
    @Environment(EnvType.CLIENT)
    private String getError() {
        String error = null;
        if (markers.size() < 2) {
            error = "atLeastTwo";
        } else {
            for (ReputationMarker marker : markers) {
                if (marker.getValue() == 0) {
                    error = "notZero";
                } else {
                    for (ReputationMarker marker2 : markers) {
                        if (!marker.equals(marker2) && marker.getValue() == marker2.getValue()) {
                            error = "unique";
                            break;
                        }
                    }
                }
                
                if (error != null) {
                    break;
                }
            }
        }
        
        return error == null ? null : I18n.get("hqm.rep." + error);
    }
    
    public ReputationMarker getCurrentMarker(int value) {
        ReputationMarker current = neutral;
        if (value != 0) {
            for (ReputationMarker marker : markers) {
                if (value > 0 && marker.getValue() > 0 && value >= marker.getValue()) {
                    current = marker;
                } else if (value < 0 && marker.getValue() < 0 && value <= marker.getValue()) {
                    current = marker;
                    break;
                }
            }
        }
        return current;
    }
    
    @Environment(EnvType.CLIENT)
    private boolean drawPointer(PoseStack matrices, GuiQuestBook gui, int value, int x, int y, int offsetY, int srcX, int mX, int mY, boolean selectedTexture) {
        boolean flag = false;
        int pointerX = x + BAR_X - ARROW_SIZE / 2 + getPointerPosition(value, true);
        int pointerY = y + BAR_Y + offsetY;
        if (gui.inBounds(pointerX, pointerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
            srcX += ARROW_SIZE;
            flag = true;
        }
        gui.drawRect(matrices, pointerX, pointerY, srcX, ARROW_SRC_Y + (selectedTexture ? -ARROW_SIZE : 0), ARROW_SIZE, ARROW_SIZE);
        
        return flag;
    }
    
    private int getPointerPosition(int value, boolean onMarker) {
        int pointerPosition = BAR_WIDTH - 1;
        
        for (int i = 0; i < markers.size(); i++) {
            ReputationMarker marker = markers.get(i);
            
            if (value <= marker.getValue()) {
                boolean pointAtMarker = onMarker && value == marker.getValue();
                
                if (pointAtMarker) {
                    pointerPosition = ARROW_MARKER_OFFSET + i * (BAR_WIDTH - ARROW_MARKER_OFFSET * 2) / (markers.size() - 1);
                } else if (i == markers.size() - 1 && marker.getValue() == value) {
                    pointerPosition = BAR_WIDTH - 1;
                } else if (i == 0) {
                    pointerPosition = 0;
                } else {
                    ReputationMarker prevMarker = markers.get(i - 1);
                    int span = marker.getValue() - prevMarker.getValue();
                    int internalValue = value - prevMarker.getValue();
                    pointerPosition = ARROW_MARKER_OFFSET + (int) ((i - 1 + ((float) internalValue / span)) * (BAR_WIDTH - ARROW_MARKER_OFFSET * 2) / (markers.size() - 1));
                }
                
                break;
            }
        }
        return pointerPosition;
    }
    
    @Environment(EnvType.CLIENT)
    public ReputationMarker onActiveClick(GuiQuestBook gui, int x, int y, int mX, int mY) {
        if (getError() != null) return null;
        
        int pointerX = x + BAR_X - ARROW_SIZE / 2 + getPointerPosition(0, true);
        int pointerY = y + BAR_Y + ARROW_MARKER_Y;
        if (gui.inBounds(pointerX, pointerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
            return neutral;
        } else {
            for (int i = 0; i < markers.size(); i++) {
                int position = i * (BAR_WIDTH - ARROW_MARKER_OFFSET * 2) / (markers.size() - 1);
                int markerX = x + BAR_X - ARROW_SIZE / 2 + position + ARROW_MARKER_OFFSET;
                int markerY = y + BAR_Y + ARROW_MARKER_Y;
                if (gui.inBounds(markerX, markerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
                    return markers.get(i);
                }
            }
            return null;
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void clearMarkers() {
        markers.clear();
        sort();
    }
    
    public void add(ReputationMarker marker) {
        markers.add(marker);
        sort();
    }
    
    public ReputationMarker getMarker(int i) {
        if (i == markers.size()) {
            return neutral;
        } else {
            return markers.get(i);
        }
    }
    
    public boolean isValid() {
        return markers.size() >= 2;
    }
    
    public int getMarkerCount() {
        return markers.size();
    }
    
    
}
