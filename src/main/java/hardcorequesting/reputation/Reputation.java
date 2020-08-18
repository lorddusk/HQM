package hardcorequesting.reputation;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.client.interfaces.edit.GuiEditMenuReputationValue;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.reward.ReputationReward;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskReputation;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StringVisitable;

import java.io.IOException;
import java.util.*;

import static hardcorequesting.client.interfaces.GuiQuestBook.selectedReputation;

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
    private static Map<String, Reputation> reputationMap = new HashMap<>();
    private String uuid;
    private String name;
    private ReputationMarker neutral;
    private List<ReputationMarker> markers;
    
    public Reputation(String name, String neutralName) {
        do {
            this.uuid = UUID.randomUUID().toString();
        } while (reputationMap.containsKey(this.uuid));
        this.name = name;
        this.neutral = new ReputationMarker(neutralName, 0, true);
        this.markers = new ArrayList<>();
    }
    
    public Reputation(String id, String name, String neutralName) {
        this.uuid = id;
        while (this.uuid == null || reputationMap.containsKey(this.uuid)) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.name = name;
        this.neutral = new ReputationMarker(neutralName, 0, true);
        this.markers = new ArrayList<>();
    }
    
    public static Map<String, Reputation> getReputations() {
        return reputationMap;
    }
    
    public static List<Reputation> getReputationList() {
        return new ArrayList<>(reputationMap.values());
    }
    
    public static Reputation getReputation(String id) {
        return reputationMap.get(id);
    }
    
    public static void clear() {
        reputationMap.clear();
    }
    
    public static void addReputation(Reputation reputation) {
        reputationMap.put(reputation.getId(), reputation);
    }
    
    public static int size() {
        return reputationMap.size();
    }
    
    @Environment(EnvType.CLIENT)
    public static void drawAll(MatrixStack matrices, GuiQuestBook gui, int x, int y, int mX, int mY, final PlayerEntity player) {
        String info = null;
        
        List<Reputation> reputations = getReputationList();
        
        Collections.sort(reputations, (reputation1, reputation2) -> Integer.compare(Math.abs(reputation2.getValue(player)), Math.abs(reputation1.getValue(player))));
        
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
    public static void drawEditPage(MatrixStack matrices, GuiQuestBook gui, int mX, int mY) {
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationMap.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationMap.size());
            List<Reputation> reputationList = getReputationList();
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
            StringVisitable neutralName = Translator.translated("hqm.rep.neutral", selectedReputation.neutral.getName());
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
    public static void onClick(GuiQuestBook gui, int mX, int mY, PlayerEntity player) {
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationMap.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationMap.size());
            List<Reputation> reputationList = getReputationList();
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
                        gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, reputation));
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        for (Quest quest : Quest.getQuests().values()) {
                            for (QuestTask task : quest.getTasks()) {
                                if (task instanceof QuestTaskReputation) {
                                    QuestTaskReputation reputationTask = (QuestTaskReputation) task;
                                    QuestTaskReputation.ReputationSetting[] settings = reputationTask.getSettings();
                                    for (int j = settings.length - 1; j >= 0; j--) {
                                        QuestTaskReputation.ReputationSetting setting = settings[j];
                                        if (reputation.equals(setting.getReputation())) {
                                            reputationTask.removeSetting(j);
                                        }
                                    }
                                }
                            }
                            
                            List<ReputationReward> rewards = quest.getReputationRewards();
                            if (rewards != null) {
                                for (Iterator<ReputationReward> iterator = rewards.iterator(); iterator.hasNext(); ) {
                                    ReputationReward reward = iterator.next();
                                    if (reputation.equals(reward.getReward())) {
                                        iterator.remove();
                                    }
                                }
                            }
                            
                        }
                        
                        reputationMap.remove(reputation.getId());
                        SaveHelper.add(SaveHelper.EditType.REPUTATION_REMOVE);
                    }
                    return;
                }
                
            }
        }
        
        if (selectedReputation != null) {
            StringVisitable neutralName = Translator.translated("hqm.rep.neutral", selectedReputation.neutral.getName());
            if (gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.RENAME) {
                    gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, selectedReputation.neutral));
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
                        gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, selectedReputation.markers.get(i)));
                    } else if (gui.getCurrentMode() == EditMode.REPUTATION_VALUE) {
                        gui.setEditMenu(new GuiEditMenuReputationValue(gui, player, selectedReputation.markers.get(i)));
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        for (Quest quest : Quest.getQuests().values()) {
                            for (QuestTask task : quest.getTasks()) {
                                if (task instanceof QuestTaskReputation) {
                                    QuestTaskReputation reputationTask = (QuestTaskReputation) task;
                                    for (QuestTaskReputation.ReputationSetting setting : reputationTask.getSettings()) {
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
                        SaveHelper.add(SaveHelper.EditType.REPUTATION_MARKER_REMOVE);
                    }
                    
                    return;
                }
            }
        }
    }
    
    public static void loadAll(boolean remote) {
        reputationMap.clear();
        try {
            SaveHandler.loadReputations(SaveHandler.getFile("reputations", remote)).forEach(Reputation::addReputation);
        } catch (IOException ignored) {
            HardcoreQuesting.LOG.info("Failed loading reputations from remote");
        }
    }
    
    public static void saveAll() {
        try {
            SaveHandler.saveReputations(SaveHandler.getLocalFile("reputations"));
        } catch (IOException e) {
            HardcoreQuesting.LOG.info("Failed saving reputations to local file");
        }
    }
    
    public static void saveAllDefault() {
        try {
            SaveHandler.saveReputations(SaveHandler.getDefaultFile("reputations"));
        } catch (IOException e) {
            HardcoreQuesting.LOG.info("Failed saving reputations to the default file");
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
    
    public int getValue(PlayerEntity player) {
        return getValue(player.getUuid());
    }
    
    public int getValue(UUID playerID) {
        return QuestingData.getQuestingData(playerID).getTeam().getReputation(this);
    }
    
    @Environment(EnvType.CLIENT)
    public String draw(MatrixStack matrices, GuiQuestBook gui, int x, int y, int mX, int mY, String info, PlayerEntity player, boolean effects, ReputationMarker lower, ReputationMarker upper, boolean inverted, ReputationMarker active, String text, boolean completed) {
        String error = getError();
        
        if (error != null) {
            gui.drawRect(x + BAR_X, y + BAR_Y, BAR_SRC_X, BAR_SRC_Y, BAR_WIDTH, BAR_HEIGHT);
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
        
        gui.drawRect(x + BAR_X, y + BAR_Y, BAR_SRC_X, normalSrcY, BAR_WIDTH, BAR_HEIGHT);
        if (effects) {
            int leftX = getPointerPosition(lowerValue, lowerOnMarker);
            if (lowerMoved) {
                leftX += lowerMovedInner ? 1 : 1 + ARROW_MARKER_OFFSET;
            }
            int rightX = getPointerPosition(upperValue, upperOnMarker) + 1;
            if (upperMoved) {
                rightX -= upperMovedInner ? 1 : ARROW_MARKER_OFFSET;
            }
            gui.drawRect(x + BAR_X + leftX, y + BAR_Y, BAR_SRC_X + leftX, selectedSrcY, rightX - leftX, BAR_HEIGHT);
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
            gui.drawRect(markerX, markerY, srcX, ARROW_SRC_Y + (selected ? -ARROW_SIZE : 0), ARROW_SIZE, ARROW_SIZE);
        }
        
        ReputationMarker current = null;
        int value = 0;
        if (player != null) {
            value = getValue(player);
            current = getCurrentMarker(value);
            
            
            if (drawPointer(gui, value, x, y, ARROW_POINTER_Y, ARROW_SRC_POINTER_X, mX, mY, false)) {
                info = current.getName() + " (" + value + ")";
            }
        }
        if (drawPointer(gui, 0, x, y, ARROW_MARKER_Y, ARROW_SRC_NEUTRAL_X, mX, mY, neutral.equals(active) || (effects && ((lowerValue <= 0 && 0 <= upperValue) != inverted)))) {
            info = neutral.getName();
        }
        
        String str;
        boolean selected = false;
        
        if (text != null) {
            str = text;
        } else if (current == null || lower != null || upper != null) {
            if (lower == null && upper == null) {
                str = GuiColor.RED + I18n.translate("hqm.rep" + (inverted ? "no" : "any") + "ValueOf") + " " + name;
                
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
                            str = I18n.translate("hqm.rep.not") + " (" + lowerName + " <= " + name + " <= " + upperName + ")";
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
        
        return error == null ? null : I18n.translate("hqm.rep." + error);
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
    private boolean drawPointer(GuiQuestBook gui, int value, int x, int y, int offsetY, int srcX, int mX, int mY, boolean selectedTexture) {
        boolean flag = false;
        int pointerX = x + BAR_X - ARROW_SIZE / 2 + getPointerPosition(value, true);
        int pointerY = y + BAR_Y + offsetY;
        if (gui.inBounds(pointerX, pointerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
            srcX += ARROW_SIZE;
            flag = true;
        }
        gui.drawRect(pointerX, pointerY, srcX, ARROW_SRC_Y + (selectedTexture ? -ARROW_SIZE : 0), ARROW_SIZE, ARROW_SIZE);
        
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
