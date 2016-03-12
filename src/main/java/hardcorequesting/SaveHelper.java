package hardcorequesting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SaveHelper {

    public enum EditType {
        QUEST_CREATE(BaseEditType.ADD, Type.QUEST),
        QUEST_REMOVE(BaseEditType.REMOVE, Type.QUEST),
        TASK_CREATE(BaseEditType.ADD, Type.TASK),
        TASK_REMOVE(BaseEditType.REMOVE, Type.TASK),
        TASK_CHANGE_TYPE(BaseEditType.ADD, Type.TASK_TYPE),
        REQUIREMENT_CHANGE(BaseEditType.CHANGE, Type.REQUIREMENT),
        REQUIREMENT_REMOVE(BaseEditType.REMOVE, Type.REQUIREMENT),
        REPEATABILITY_CHANGED(BaseEditType.CHANGE, Type.REPEATABILITY),
        VISIBILITY_CHANGED(BaseEditType.CHANGE, Type.VISIBILITY),
        PARENT_REQUIREMENT_CHANGED(BaseEditType.CHANGE, Type.PARENT),
        OPTION_CHANGE(BaseEditType.ADD, Type.OPTION),
        OPTION_REMOVE(BaseEditType.REMOVE, Type.OPTION),
        NAME_CHANGE(BaseEditType.CHANGE, Type.NAME),
        DESCRIPTION_CHANGE(BaseEditType.CHANGE, Type.DESCRIPTION),
        ICON_CHANGE(BaseEditType.CHANGE, Type.ICON),
        QUEST_SIZE_CHANGE(BaseEditType.CHANGE, Type.QUEST_SIZE),
        QUEST_MOVE(BaseEditType.MOVE, Type.QUEST),
        QUEST_CHANGE_SET(BaseEditType.MOVE, Type.BETWEEN_SETS),
        SET_CREATE(BaseEditType.ADD, Type.SET),
        SET_REMOVE(BaseEditType.REMOVE, Type.SET),
        REWARD_CREATE(BaseEditType.ADD, Type.REWARD),
        REWARD_CHANGE(BaseEditType.CHANGE, Type.REWARD),
        REWARD_REMOVE(BaseEditType.REMOVE, Type.REWARD),
        MONSTER_CREATE(BaseEditType.ADD, Type.MONSTER),
        MONSTER_CHANGE(BaseEditType.CHANGE, Type.MONSTER),
        MONSTER_REMOVE(BaseEditType.REMOVE, Type.MONSTER),
        LOCATION_CREATE(BaseEditType.ADD, Type.LOCATION),
        LOCATION_CHANGE(BaseEditType.CHANGE, Type.LOCATION),
        LOCATION_REMOVE(BaseEditType.REMOVE, Type.LOCATION),
        TIER_CREATE(BaseEditType.ADD, Type.TIER),
        TIER_CHANGE(BaseEditType.CHANGE, Type.TIER),
        TIER_REMOVE(BaseEditType.REMOVE, Type.TIER),
        GROUP_CREATE(BaseEditType.ADD, Type.GROUP),
        GROUP_CHANGE(BaseEditType.CHANGE, Type.GROUP),
        GROUP_REMOVE(BaseEditType.REMOVE, Type.GROUP),
        GROUP_ITEM_CREATE(BaseEditType.ADD, Type.GROUP_ITEM),
        GROUP_ITEM_CHANGE(BaseEditType.REMOVE, Type.GROUP_ITEM),
        GROUP_ITEM_REMOVE(BaseEditType.REMOVE, Type.GROUP_ITEM),
        DEATH_CHANGE(BaseEditType.CHANGE, Type.DEATH),
        TASK_ITEM_CREATE(BaseEditType.ADD, Type.TASK_ITEM),
        TASK_ITEM_CHANGE(BaseEditType.CHANGE, Type.TASK_ITEM),
        TASK_ITEM_REMOVE(BaseEditType.REMOVE, Type.TASK_ITEM),
        REPUTATION_ADD(BaseEditType.ADD, Type.REPUTATION),
        REPUTATION_REMOVE(BaseEditType.REMOVE, Type.REPUTATION),
        REPUTATION_MARKER_CREATE(BaseEditType.ADD, Type.REPUTATION_MARKER),
        REPUTATION_MARKER_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_MARKER),
        REPUTATION_MARKER_REMOVE(BaseEditType.REMOVE, Type.REPUTATION_MARKER),
        REPUTATION_TASK_CREATE(BaseEditType.ADD, Type.REPUTATION_TASK),
        REPUTATION_TASK_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_TASK),
        REPUTATION_TASK_REMOVE(BaseEditType.REMOVE, Type.REPUTATION_TASK),
        REPUTATION_REWARD_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_REWARD),
        KILLS_CHANGE(BaseEditType.CHANGE, Type.KILLS),
        REPUTATION_BAR_ADD(BaseEditType.ADD, Type.REPUTATION_BAR),
        REPUTATION_BAR_MOVE(BaseEditType.MOVE, Type.REPUTATION_BAR),
        REPUTATION_BAR_CHANGE(BaseEditType.CHANGE, Type.REPUTATION_BAR),
        REPUTATION_BAR_REMOVE(BaseEditType.REMOVE, Type.REPUTATION_BAR),
        COMMAND_ADD(BaseEditType.ADD, Type.COMMAND),
        COMMAND_CHANGE(BaseEditType.CHANGE, Type.COMMAND),
        COMMAND_REMOVE(BaseEditType.REMOVE, Type.COMMAND);

        private BaseEditType basType;
        private Type type;

        EditType(BaseEditType basType, Type type) {
            this.basType = basType;
            this.type = type;
        }

        public String translate(int number) {
            return basType.translate() + " " + type.translate() + ": " + basType.colour + number;
        }

        private enum BaseEditType {
            ADD("added", GuiColor.GREEN),
            CHANGE("changed", GuiColor.ORANGE),
            MOVE("moved", GuiColor.ORANGE),
            REMOVE("removed", GuiColor.RED);

            private String id;
            private GuiColor colour;

            BaseEditType(String id, GuiColor colour) {
                this.id = id;
                this.colour = colour;
            }

            private String translate() {
                return Translator.translate("hqm.editType." + id);
            }
        }

        private enum Type {
            QUEST("quest"),
            TASK("task"),
            TASK_TYPE("taskType"),
            REQUIREMENT("req"),
            REPEATABILITY("repeat"),
            VISIBILITY("vis"),
            PARENT("parent"),
            OPTION("option"),
            NAME("name"),
            DESCRIPTION("desc"),
            ICON("icon"),
            QUEST_SIZE("questSize"),
            SET("set"),
            REWARD("reward"),
            MONSTER("monster"),
            LOCATION("location"),
            TIER("tier"),
            GROUP("group"),
            GROUP_ITEM("groupItem"),
            DEATH("death"),
            TASK_ITEM("taskItem"),
            REPUTATION("rep"),
            REPUTATION_MARKER("repMark"),
            REPUTATION_TASK("repTask"),
            REPUTATION_REWARD("repReward"),
            KILLS("kills"),
            REPUTATION_BAR("repBar"),
            BETWEEN_SETS("betweenSets"),
            COMMAND("command");

            private String id;

            Type(String id) {
                this.id = id;
            }

            private String translate() {
                return Translator.translate("hqm.editType." + id);
            }
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
            return ((Integer) o.count).compareTo(count);
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
        } else {
            gui.drawRect(X, Y, SMALL_SRC_X, SMALL_SRC_Y, SMALL_SIZE, SMALL_SIZE);
        }

        int indexX = isLarge ? 0 : 1;
        int indexY = gui.inBounds(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SIZE, CHANGE_SIZE, mX, mY) ? 1 : 0;
        gui.drawRect(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SRC_X + indexX * CHANGE_SIZE, SRC_Y + indexY * CHANGE_SIZE, CHANGE_SIZE, CHANGE_SIZE);

        if (isLarge) {
            if (total == 0) {
                gui.drawString(Translator.translate("hqm.editType.allSaved"), X + START_X, Y + START_Y, 0.7F, 0x404040);
            } else {
                if (saveTime == 0) {
                    gui.drawString(Translator.translate("hqm.editType.neverSaved"), X + START_X, Y + START_Y, 0.7F, 0x404040);
                } else {
                    gui.drawString(formatTime((int) ((Minecraft.getSystemTime() - saveTime) / 60000)), X + START_X, Y + START_Y, 0.7F, 0x404040);
                }

                gui.drawString(Translator.translate("hqm.editType.unsaved", total), X + START_X, Y + START_Y + 2 * FONT_HEIGHT, 0.7F, 0x404040);
                int others = total;
                for (int i = 0; i < LISTED_TYPES; i++) {
                    ListElement element = sortedList.get(i);
                    if (element.count == 0) {
                        //since it's sorted, the first 0 means the rest is empty
                        break;
                    }
                    gui.drawString(element.type.translate(element.count), X + START_X + INDENT, Y + START_Y + (i + 3) * FONT_HEIGHT, 0.7F, 0x404040);
                    others -= element.count;
                }
                if (others > 0) {
                    gui.drawString(Translator.translate("hqm.editType.other", others), X + START_X + INDENT, Y + START_Y + (LISTED_TYPES + 3) * FONT_HEIGHT, 0.7F, 0x404040);
                }
            }
        } else {
            int index = inSaveBounds(gui, mX, mY) ? 1 : 0;
            gui.drawRect(X + SAVE_X, Y + SAVE_Y, SAVE_SRC_X + index * SAVE_SIZE, SRC_Y, SAVE_SIZE, SAVE_SIZE);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void onClick(GuiQuestBook gui, int mX, int mY) {
        if (gui.inBounds(X + CHANGE_X, Y + CHANGE_Y, CHANGE_SIZE, CHANGE_SIZE, mX, mY)) {
            isLarge = !isLarge;
        } else if (inSaveBounds(gui, mX, mY)) {
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
                return Translator.translate("hqm.editType.savedRecent");
            } else {
                return Translator.translate(minutes != 1, "hqm.editType.savedMinutes", minutes);
            }
        } else {
            return Translator.translate(hours != 1, "hqm.editType.savedMinutes", hours);
        }
    }


    public static boolean isLarge() {
        return isLarge;
    }

    private SaveHelper() {
    }
}
