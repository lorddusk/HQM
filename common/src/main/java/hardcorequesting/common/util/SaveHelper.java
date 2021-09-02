package hardcorequesting.common.util;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SaveHelper {
    
    private static final int START_X = 5;
    private static final int START_Y = 30;
    private static final int INDENT = 5;
    private static final int FONT_HEIGHT = 7;
    private static final int LISTED_TYPES = 7;
    private static final int X = 342;
    private static final int Y = 5;
    private static final int SRC_X = 0;
    private static final int SRC_Y = 123;
    private static final int WIDTH = 104;
    private static final int HEIGHT = 110;
    private static final int CHANGE_X = 4;
    private static final int CHANGE_Y = 4;
    private static final int CHANGE_SIZE = 7;
    private static final int CHANGE_SRC_X = 104;
    private static final int SMALL_SRC_X = 104;
    private static final int SMALL_SRC_Y = 137;
    private static final int SMALL_SIZE = 29;
    private static final int SAVE_X = 11;
    private static final int SAVE_Y = 11;
    private static final int SAVE_SRC_X = 118;
    private static final int SAVE_SIZE = 14;
    private static boolean isLarge = true;
    private static long saveTime;
    private static int total;
    public static ListElement[] list;
    private static List<ListElement> sortedList;
    
    static {
        createList();
    }
    
    private SaveHelper() {
    }
    
    private static void createList() {
        list = new ListElement[EditType.values().length];
        for (int i = 0; i < list.length; i++) {
            list[i] = new ListElement(EditType.values()[i]);
        }
        sortedList = new ArrayList<>();
        Collections.addAll(sortedList, list);
        Collections.sort(sortedList);
        total = 0;
    }
    
    public static void add(EditType type, int count) {
        list[type.ordinal()].count += count;
        Collections.sort(sortedList);
        total += count;
    }
    
    public static void add(EditType type) {
        add(type, 1);
    }
    
    public static void onSave() {
        saveTime = System.currentTimeMillis();
        createList();
    }
    
    public static void onLoad() {
        createList();
    }
    
    @Environment(EnvType.CLIENT)
    public static void render(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        if (isLarge) {
            gui.drawRect(matrices, X, Y, SRC_X, SRC_Y, WIDTH, HEIGHT);
        } else {
            gui.drawRect(matrices, X, Y, SMALL_SRC_X, SMALL_SRC_Y, SMALL_SIZE, SMALL_SIZE);
        }
        
        int indexX = isLarge ? 0 : 1;
        int indexY = gui.inBounds(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SIZE, CHANGE_SIZE, mX, mY) ? 1 : 0;
        gui.drawRect(matrices, X + CHANGE_X, Y + CHANGE_Y, CHANGE_SRC_X + indexX * CHANGE_SIZE, SRC_Y + indexY * CHANGE_SIZE, CHANGE_SIZE, CHANGE_SIZE);
        
        if (isLarge) {
            if (total == 0) {
                gui.drawString(matrices, Translator.translatable("hqm.editType.allSaved"), X + START_X, Y + START_Y, 0.7F, 0x404040);
            } else {
                if (saveTime == 0) {
                    gui.drawString(matrices, Translator.translatable("hqm.editType.neverSaved"), X + START_X, Y + START_Y, 0.7F, 0x404040);
                } else {
                    gui.drawString(matrices, formatTime((int) ((System.currentTimeMillis() - saveTime) / 60000)), X + START_X, Y + START_Y, 0.7F, 0x404040);
                }
                
                gui.drawString(matrices, Translator.translatable("hqm.editType.unsaved", total), X + START_X, Y + START_Y + 2 * FONT_HEIGHT, 0.7F, 0x404040);
                int others = total;
                for (int i = 0; i < LISTED_TYPES; i++) {
                    ListElement element = sortedList.get(i);
                    if (element.count == 0) {
                        //since it's sorted, the first 0 means the rest is empty
                        break;
                    }
                    gui.drawString(matrices, Translator.plain(element.type.translate(element.count)), X + START_X + INDENT, Y + START_Y + (i + 3) * FONT_HEIGHT, 0.7F, 0x404040);
                    others -= element.count;
                }
                if (others > 0) {
                    gui.drawString(matrices, Translator.translatable("hqm.editType.other", others), X + START_X + INDENT, Y + START_Y + (LISTED_TYPES + 3) * FONT_HEIGHT, 0.7F, 0x404040);
                }
            }
        } else {
            int index = inSaveBounds(gui, mX, mY) ? 1 : 0;
            gui.drawRect(matrices, X + SAVE_X, Y + SAVE_Y, SAVE_SRC_X + index * SAVE_SIZE, SRC_Y, SAVE_SIZE, SAVE_SIZE);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static void onClick(GuiQuestBook gui, int mX, int mY) {
        if (gui.inBounds(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SIZE, CHANGE_SIZE, mX, mY)) {
            isLarge = !isLarge;
        } else if (inSaveBounds(gui, mX, mY)) {
            gui.save();
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static boolean inSaveBounds(GuiQuestBook gui, int mX, int mY) {
        return !isLarge && gui.inBounds(X + SAVE_X, Y + SAVE_Y, SAVE_SIZE, SAVE_SIZE, mX, mY);
    }
    
    private static FormattedText formatTime(int minutes) {
        int hours = minutes / 60;
        minutes -= hours * 60;
        
        if (hours == 0) {
            if (minutes == 0) {
                return Translator.translatable("hqm.editType.savedRecent");
            } else {
                return Translator.pluralTranslated(minutes != 1, "hqm.editType.savedMinutes", minutes);
            }
        } else {
            return Translator.pluralTranslated(hours != 1, "hqm.editType.savedMinutes", hours);
        }
    }
    
    public static boolean isLarge() {
        return isLarge;
    }
    
    public static class ListElement implements Comparable<ListElement> {
        
        private EditType type;
        public int count;
        
        private ListElement(EditType type) {
            this.type = type;
            this.count = 0;
        }
        
        @Override
        public int compareTo(ListElement o) {
            return Integer.compare(o.count, count);
        }
    }
}
