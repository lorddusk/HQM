package hardcorequesting.client;

import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Environment(EnvType.CLIENT)
public class KeyboardHandler {
    
    private static Map<Integer, Set<EditMode>> keyMap;
    
    public static void initDefault() {
        addKeymap(GLFW.GLFW_KEY_M, EditMode.MOVE);
        addKeymap(GLFW.GLFW_KEY_R, EditMode.RENAME);
        addKeymap(GLFW.GLFW_KEY_N, EditMode.CREATE);
        addKeymap(GLFW.GLFW_KEY_INSERT, EditMode.CREATE);
        addKeymap(GLFW.GLFW_KEY_DELETE, EditMode.DELETE);
        addKeymap(GLFW.GLFW_KEY_D, EditMode.DELETE);
        addKeymap(GLFW.GLFW_KEY_S, EditMode.SWAP_SELECT);
        addKeymap(GLFW.GLFW_KEY_S, EditMode.SWAP);
        addKeymap(GLFW.GLFW_KEY_SPACE, EditMode.NORMAL);
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
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            gui.goBack();
            return true;
        } else if (Quest.canQuestsBeEdited()) {
            if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_0) {
                int i = key - GLFW.GLFW_KEY_1;
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
                list.add(entry.getKey() + ":" + mode.name().toLowerCase());
        return list.toArray(new String[0]);
    }
    
    public static void fromConfig(String[] config) {
        for (String entry : config) {
            String[] splitted = entry.split(":");
            if (splitted.length != 2) continue;
            int key = Integer.parseInt(splitted[0]);
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
