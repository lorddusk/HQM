package hardcorequesting;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SaveHelper {



    public static enum EditType {
        QUEST_CREATE("Created quests", GuiColor.GREEN),
        QUEST_REMOVE("Removed quests", GuiColor.RED),
        TASK_CREATE("Created tasks", GuiColor.GREEN),
        TASK_REMOVE("Removed tasks", GuiColor.RED),
        TASK_CHANGE_TYPE("Changed task type", GuiColor.ORANGE),
        REQUIREMENT_CHANGE("Added requirements", GuiColor.GREEN),
        REQUIREMENT_REMOVE("Removed requirements", GuiColor.RED),
        REPEATABILITY_CHANGED("Changed repeatability", GuiColor.ORANGE),
        VISIBILITY_CHANGED("Changed triggers", GuiColor.ORANGE),
        PARENT_REQUIREMENT_CHANGED("Changed parent count", GuiColor.ORANGE),
        OPTION_CHANGE("Added quest options", GuiColor.GREEN),
        OPTION_REMOVE("Removed quest options", GuiColor.RED),
        NAME_CHANGE("Changed names", GuiColor.ORANGE),
        DESCRIPTION_CHANGE("Changed descriptions", GuiColor.ORANGE),
        ICON_CHANGE("Changed quest icons", GuiColor.ORANGE),
        QUEST_SIZE_CHANGE("Changed quest sizes", GuiColor.ORANGE),
        QUEST_MOVE("Moved quests", GuiColor.ORANGE),
        QUEST_CHANGE_SET("Moved between sets", GuiColor.ORANGE),
        SET_CREATE("Created quest sets", GuiColor.GREEN),
        SET_REMOVE("Removed quest sets", GuiColor.RED),
        REWARD_CREATE("Created rewards", GuiColor.GREEN),
        REWARD_CHANGE("Changed rewards", GuiColor.ORANGE),
        REWARD_REMOVE("Removed rewards", GuiColor.RED),
        MONSTER_CREATE("Created mobs", GuiColor.GREEN),
        MONSTER_CHANGE("Changed mobs", GuiColor.ORANGE),
        MONSTER_REMOVE("Removed mobs", GuiColor.RED),
        LOCATION_CREATE("Created locations", GuiColor.GREEN),
        LOCATION_CHANGE("Changed locations", GuiColor.ORANGE),
        LOCATION_REMOVE("Removed locations", GuiColor.RED),
        TIER_CREATE("Created tiers", GuiColor.GREEN),
        TIER_CHANGE("Changed tiers", GuiColor.ORANGE),
        TIER_REMOVE("Removed tiers", GuiColor.RED),
        GROUP_CREATE("Created groups", GuiColor.GREEN),
        GROUP_CHANGE("Changed groups", GuiColor.ORANGE),
        GROUP_REMOVE("Removed groups", GuiColor.RED),
        GROUP_ITEM_CREATE("Created group items", GuiColor.GREEN),
        GROUP_ITEM_CHANGE("Changed group items", GuiColor.ORANGE),
        GROUP_ITEM_REMOVE("Removed group items", GuiColor.RED),
        DEATH_CHANGE("Changed deaths", GuiColor.ORANGE),
        TASK_ITEM_CREATE("Created task items", GuiColor.GREEN),
        TASK_ITEM_CHANGE("Changed task items", GuiColor.ORANGE),
        TASK_ITEM_REMOVE("Removed task items", GuiColor.RED),
        REPUTATION_ADD("Created reputations", GuiColor.GREEN),
        REPUTATION_REMOVE("Removed reputations", GuiColor.RED),
        REPUTATION_MARKER_CREATE("Created rep tiers", GuiColor.GREEN),
        REPUTATION_MARKER_CHANGE("Changed rep tiers", GuiColor.ORANGE),
        REPUTATION_MARKER_REMOVE("Removed rep tiers", GuiColor.RED),
        REPUTATION_TASK_CREATE("Created rep targets", GuiColor.GREEN),
        REPUTATION_TASK_CHANGE("Changed rep targets", GuiColor.ORANGE),
        REPUTATION_TASK_REMOVE("Removed rep targets", GuiColor.RED),
        REPUTATION_REWARD_CHANGE("Changed rep rewards", GuiColor.ORANGE),
        KILLS_CHANGE("Changed kills", GuiColor.ORANGE);


        private String label;
        private GuiColor color;

        EditType(String label, GuiColor color) {
            this.label = label;
            this.color = color;
        }
    }



    public static class ListElement implements Comparable<ListElement> {
        private EditType type;
        private int count;

        private ListElement(EditType type) {
            this.type = type;
            this.count = 0;
        }

        @Override
        public int compareTo(ListElement o) {
            return ((Integer)o.count).compareTo(count);
        }
    }

    private static boolean isLarge = true;
    private static long saveTime;
    private static int total;
    private static ListElement[] list;
    private static List<ListElement> sortedList;

    private static void createList() {
        list = new ListElement[EditType.values().length];
        for (int i = 0; i < list.length; i++) {
            list[i] = new ListElement(EditType.values()[i]);
        }
        sortedList = new ArrayList<ListElement>();
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

    static {
        createList();
    }

    public static void onSave() {
        saveTime = Minecraft.getSystemTime();
        createList();
    }

    public static void onLoad() {
        createList();
    }

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

    @SideOnly(Side.CLIENT)
    public static void render(GuiQuestBook gui, int mX, int mY) {
        if (isLarge) {
            gui.drawRect(X, Y, SRC_X, SRC_Y, WIDTH, HEIGHT);
        }else{
            gui.drawRect(X, Y, SMALL_SRC_X, SMALL_SRC_Y, SMALL_SIZE, SMALL_SIZE);
        }

        int indexX = isLarge ? 0 : 1;
        int indexY = gui.inBounds(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SIZE, CHANGE_SIZE, mX, mY) ? 1 : 0;
        gui.drawRect(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SRC_X + indexX * CHANGE_SIZE, SRC_Y + indexY * CHANGE_SIZE, CHANGE_SIZE, CHANGE_SIZE);

        if (isLarge) {
            if (total == 0) {
                gui.drawString("Everything is saved!", X + START_X, Y + START_Y, 0.7F, 0x404040);
            }else{
                if (saveTime == 0) {
                    gui.drawString("Never saved this session", X + START_X, Y + START_Y, 0.7F, 0x404040);
                }else{
                    gui.drawString(formatTime((int)((Minecraft.getSystemTime() - saveTime) / 60000)), X + START_X, Y + START_Y, 0.7F, 0x404040);
                }

                gui.drawString("Unsaved changes: " + total, X + START_X, Y + START_Y + 2 * FONT_HEIGHT, 0.7F, 0x404040);
                int others = total;
                for (int i = 0; i < LISTED_TYPES; i++) {
                    ListElement element = sortedList.get(i);
                    if (element.count == 0) {
                        //since it's sorted, the first 0 means the rest is empty
                        break;
                    }
                    gui.drawString(element.type.label + ": " + element.type.color + element.count, X + START_X + INDENT, Y + START_Y + (i + 3) * FONT_HEIGHT, 0.7F, 0x404040);
                    others -= element.count;
                }
                if (others > 0) {
                    gui.drawString("Other changes: " + others, X + START_X + INDENT, Y + START_Y + (LISTED_TYPES + 3) * FONT_HEIGHT, 0.7F, 0x404040);
                }
            }
        }else{
            int index = inSaveBounds(gui, mX, mY) ? 1 : 0;
            gui.drawRect(X + SAVE_X, Y + SAVE_Y, SAVE_SRC_X + index * SAVE_SIZE, SRC_Y, SAVE_SIZE, SAVE_SIZE);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void onClick(GuiQuestBook gui, int mX, int mY) {
        if (gui.inBounds(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SIZE, CHANGE_SIZE, mX, mY)) {
            isLarge = !isLarge;
        }else if(inSaveBounds(gui, mX, mY)) {
            gui.save();
        }
    }

    @SideOnly(Side.CLIENT)
    public static boolean inSaveBounds(GuiQuestBook gui, int mX, int mY) {
        return !isLarge && gui.inBounds(X + SAVE_X, Y + SAVE_Y, SAVE_SIZE, SAVE_SIZE, mX, mY);
    }


    private static String formatTime(int minutes) {
        int hours = minutes / 60;
        minutes -= hours * 60;

        if (hours == 0) {
            if (minutes == 0) {
                return "Saved recently";
            }else if (minutes == 1){
                return "Saved 1 minute ago";
            }else{
                return "Saved " + minutes + " minutes ago";
            }
        }else if (hours == 1){
            return "Saved 1 hour ago";
        }else{
            return "Saved " + hours + " hours ago";
        }
    }


    public static boolean isLarge() {
        return isLarge;
    }

    private SaveHelper() {}
}
