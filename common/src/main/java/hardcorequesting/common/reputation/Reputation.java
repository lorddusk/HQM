package hardcorequesting.common.reputation;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.util.Translator;
import hardcorequesting.common.util.WrappedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Reputation {
    public static final int BAR_SRC_X = 0;
    public static final int BAR_SRC_Y = 101;
    public static final int BAR_WIDTH = 125;
    public static final int BAR_HEIGHT = 3;
    public static final int BAR_X = 0;
    public static final int BAR_Y = 5;
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
    private WrappedText name;
    private final ReputationMarker neutral;
    private final List<ReputationMarker> markers;
    
    public Reputation() {
        Map<String, Reputation> reputationMap = ReputationManager.getInstance().reputationMap;
        do {
            this.uuid = UUID.randomUUID().toString();
        } while (reputationMap.containsKey(this.uuid));
        this.name = WrappedText.create("Unnamed");
        this.neutral = new ReputationMarker("Neutral", 0, true);
        this.markers = new ArrayList<>();
    }
    
    public Reputation(String id, WrappedText name, String neutralName) {
        Map<String, Reputation> reputationMap = ReputationManager.getInstance().reputationMap;
        this.uuid = id;
        while (this.uuid == null || reputationMap.containsKey(this.uuid)) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.name = name;
        this.neutral = new ReputationMarker(neutralName, 0, true);
        this.markers = new ArrayList<>();
    }
    
    public String getId() {
        return uuid;
    }
    
    public String getNeutralName() {
        return neutral.getName();
    }
    
    public void setNeutralName(String name) {
        neutral.setName(name);
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
    public String drawAndGetTooltip(PoseStack matrices, GuiQuestBook gui, int x, int y, int mX, int mY, String info, UUID playerId, boolean effects, ReputationMarker lower, ReputationMarker upper, boolean inverted, ReputationMarker active, FormattedText text, boolean completed) {
        draw(matrices, gui, x, y, mX, mY, playerId, effects, lower, upper, inverted, active, text, completed);
        return info != null ? info : getTooltip(gui, x, y, mX, mY, playerId);
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, int x, int y, int mX, int mY, UUID playerId, boolean effects, ReputationMarker lower, ReputationMarker upper, boolean inverted, ReputationMarker active, FormattedText text, boolean completed) {
        String error = getError();
        
        if (error != null) {
            gui.drawRect(matrices, x + BAR_X, y + BAR_Y, BAR_SRC_X, BAR_SRC_Y, BAR_WIDTH, BAR_HEIGHT);
            gui.drawString(matrices, Translator.plain(error), x + TEXT_X, y + TEXT_Y, 0.7F, 0xff5555);
            return;
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
            if (gui.inBounds(markerX, markerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
                srcX += ARROW_SIZE;
            }
            
            boolean selected = markers.get(i).equals(active) || (effects && ((lowerValue <= value && value <= upperValue) != inverted));
            gui.drawRect(matrices, markerX, markerY, srcX, ARROW_SRC_Y + (selected ? -ARROW_SIZE : 0), ARROW_SIZE, ARROW_SIZE);
        }
        
        ReputationMarker current = null;
        int value = 0;
        if (playerId != null) {
            value = getValue(playerId);
            current = getCurrentMarker(value);
    
    
            drawPointer(matrices, gui, value, x, y, ARROW_POINTER_Y, ARROW_SRC_POINTER_X, mX, mY, false);
        }
        drawPointer(matrices, gui, 0, x, y, ARROW_MARKER_Y, ARROW_SRC_NEUTRAL_X, mX, mY, neutral.equals(active) || (effects && ((lowerValue <= 0 && 0 <= upperValue) != inverted)));
        
        FormattedText info;
        boolean selected = false;
        
        if (text != null) {
            info = text;
        } else if (current == null || lower != null || upper != null) {
            if (lower == null && upper == null) {
                info = Translator.translatable("hqm.rep" + (inverted ? "no" : "any") + "ValueOf", name.getText())
                        .withStyle(ChatFormatting.DARK_RED);
                
            } else {
                String lowerName = lower == null ? null : Screen.hasShiftDown() ? String.valueOf(lower.getValue()) : lower.getName();
                String upperName = upper == null ? null : Screen.hasShiftDown() ? String.valueOf(upper.getValue()) : upper.getName();
                
                if (lower != null && upper != null) {
                    if (lower.equals(upper)) {
                        if (inverted) {
                            info = Translator.translatable("hqm.rep.not_equals", name, lowerName);
                        } else {
                            info = Translator.translatable("hqm.rep.equals", name, lowerName);
                        }
                    } else {
                        info = Translator.translatable("hqm.rep.between", lowerName, name.getText(), upperName);
                        if (inverted) {
                            info = Translator.translatable("hqm.rep.not", info);
                        }
                    }
                } else if (lower != null) {
                    info = Translator.translatable(inverted ? "hqm.rep.min_inverted" : "hqm.rep.min", name, lowerName);
                } else {
                    info = Translator.translatable(inverted ? "hqm.rep.max_inverted" : "hqm.rep.max", name, upperName);
                }
            }
        } else {
            info = Translator.translatable("hqm.rep.current", name, current.getName(), value);
            selected = completed || (effects && ((lowerValue <= current.getValue() && current.getValue() <= upperValue) != inverted));
        }
        
        gui.drawString(matrices, info, x + TEXT_X, y + TEXT_Y, 0.7F, selected ? 0x40AA40 : 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    public String getTooltip(GuiQuestBook gui, int x, int y, int mX, int mY, UUID playerId) {
    
        if (getError() != null) {
            return null;
        }
        
        for (int i = 0; i < markers.size(); i++) {
            int position = i * (BAR_WIDTH - ARROW_MARKER_OFFSET * 2) / (markers.size() - 1);
            int markerX = x + BAR_X - ARROW_SIZE / 2 + position + ARROW_MARKER_OFFSET;
            
            int markerY = y + BAR_Y + ARROW_MARKER_Y;
            int value = markers.get(i).getValue();
            if (gui.inBounds(markerX, markerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
                return markers.get(i).getName() + " (" + value + ")";
            }
        }
        
        if (playerId != null) {
            int value = getValue(playerId);
            ReputationMarker current = getCurrentMarker(value);
            
            if (isOnPointer(gui, value, x, y, ARROW_POINTER_Y, mX, mY)) {
                return current.getName() + " (" + value + ")";
            }
        }
        
        if (isOnPointer(gui, 0, x, y, ARROW_MARKER_Y, mX, mY)) {
            return neutral.getName();
        }
        
        return null;
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
    private void drawPointer(PoseStack matrices, GuiQuestBook gui, int value, int x, int y, int offsetY, int srcX, int mX, int mY, boolean selectedTexture) {
        int pointerX = x + BAR_X - ARROW_SIZE / 2 + getPointerPosition(value, true);
        int pointerY = y + BAR_Y + offsetY;
        if (gui.inBounds(pointerX, pointerY, ARROW_SIZE, ARROW_SIZE, mX, mY)) {
            srcX += ARROW_SIZE;
        }
        gui.drawRect(matrices, pointerX, pointerY, srcX, ARROW_SRC_Y + (selectedTexture ? -ARROW_SIZE : 0), ARROW_SIZE, ARROW_SIZE);
    }
    
    @Environment(EnvType.CLIENT)
    private boolean isOnPointer(GuiQuestBook gui, int value, int x, int y, int offsetY, int mX, int mY) {
        int pointerX = x + BAR_X - ARROW_SIZE / 2 + getPointerPosition(value, true);
        int pointerY = y + BAR_Y + offsetY;
        return gui.inBounds(pointerX, pointerY, ARROW_SIZE, ARROW_SIZE, mX, mY);
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
    
    public WrappedText getName() {
        return name;
    }
    
    public void setName(WrappedText name) {
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
    
    public void remove(ReputationMarker marker) {
        for (Quest quest : Quest.getQuests().values()) {
            for (QuestTask<?> task : quest.getTasks()) {
                task.onRemovedRepMarker(marker);
            }
        }
        
        markers.remove(marker);
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
    
    public List<ReputationMarker> getMarkers() {
        return Collections.unmodifiableList(markers);
    }
}
