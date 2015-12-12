package hardcorequesting.client;

import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.quests.Quest;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class KeyboardHandler {

    private static Map<Integer, Set<EditMode>> keyMap;

    public static void initDefault() {
        addKeymap(Keyboard.KEY_M, EditMode.MOVE);
        addKeymap(Keyboard.KEY_R, EditMode.RENAME);
        addKeymap(Keyboard.KEY_N, EditMode.CREATE);
        addKeymap(Keyboard.KEY_INSERT, EditMode.CREATE);
        addKeymap(Keyboard.KEY_DELETE, EditMode.DELETE);
        addKeymap(Keyboard.KEY_D, EditMode.DELETE);
        addKeymap(Keyboard.KEY_S, EditMode.SWAP_SELECT);
        addKeymap(Keyboard.KEY_S, EditMode.SWAP);
        addKeymap(Keyboard.KEY_SPACE, EditMode.NORMAL);
    }

    public static void clear() {
        keyMap.clear();
    }

    private static void addKeymap(int key, EditMode mode) {
        if (keyMap == null) keyMap = new HashMap<>();
        Set<EditMode> set = keyMap.get(key);
        if (set == null) set = new HashSet<>();
        set.add(mode);
        keyMap.put(key, set);
    }

    public static boolean pressedHotkey(GuiQuestBook gui, int key, EditButton[] buttons) {
        if (key == Keyboard.KEY_BACK) {
            gui.goBack();
            return true;
        } else if (Quest.isEditing) {
            if (key >= Keyboard.KEY_1 && key <= Keyboard.KEY_0) {
                int i = key - Keyboard.KEY_1;
                if (i < buttons.length) {
                    buttons[i].click();
                    return true;
                }
            } else if (keyMap.containsKey(key)) {
                Set<EditMode> modes = keyMap.get(key);
                for (EditButton button : buttons) {
                    for (EditMode mode : modes) {
                        if (button.matchesMode(mode)) {
                            button.click();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static String[] toConfig() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<Integer, Set<EditMode>> entry : keyMap.entrySet())
            for (EditMode mode : entry.getValue())
                list.add(Keyboard.getKeyName(entry.getKey()) + ":" + mode.name().toLowerCase());
        return list.toArray(new String[list.size()]);
    }

    public static void fromConfig(String[] config) {
        for (String entry : config) {
            String[] splitted = entry.split(":");
            if (splitted.length != 2) continue;
            int key = Keyboard.getKeyIndex(splitted[0]);
            if (key == Keyboard.KEY_NONE) continue;
            EditMode mode = EditMode.valueOf(splitted[1].toUpperCase());
            if (mode == null) continue;
            addKeymap(key, mode);
        }
    }

    public static String[] getDefault() {
        initDefault();
        String[] map = toConfig();
        clear();
        return map;
    }
}
