package hardcorequesting.common.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import hardcorequesting.common.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class KeyboardHandler {
    
    public static Multimap<Integer, EditMode> keyMap = Multimaps.newMultimap(Maps.newHashMap(), Lists::newArrayList);
    
    public static void initDefault() {
        addKeymap(GLFW.GLFW_KEY_M, EditMode.MOVE);
        addKeymap(GLFW.GLFW_KEY_R, EditMode.RENAME);
        addKeymap(GLFW.GLFW_KEY_N, EditMode.CREATE);
        addKeymap(GLFW.GLFW_KEY_INSERT, EditMode.CREATE);
        
        addKeymap(GLFW.GLFW_KEY_N, EditMode.TASK);
        addKeymap(GLFW.GLFW_KEY_INSERT, EditMode.TASK);
        
        addKeymap(GLFW.GLFW_KEY_DELETE, EditMode.DELETE);
        addKeymap(GLFW.GLFW_KEY_D, EditMode.DELETE);
        addKeymap(GLFW.GLFW_KEY_S, EditMode.SWAP_SELECT);
        addKeymap(GLFW.GLFW_KEY_S, EditMode.SWAP);
        addKeymap(GLFW.GLFW_KEY_R, EditMode.REQUIREMENT);
        addKeymap(GLFW.GLFW_KEY_O, EditMode.QUEST_OPTION);
        addKeymap(GLFW.GLFW_KEY_I, EditMode.ITEM);
        addKeymap(GLFW.GLFW_KEY_L, EditMode.LOCATION);
        addKeymap(GLFW.GLFW_KEY_C, EditMode.MOB);
        addKeymap(GLFW.GLFW_KEY_SPACE, EditMode.NORMAL);
        addKeymap(GLFW.GLFW_KEY_B, EditMode.BAG);
        addKeymap(GLFW.GLFW_KEY_R, EditMode.REPUTATION);
    }
    
    public static void clear() {
        if (keyMap != null) keyMap.clear();
    }
    
    private static void addKeymap(int key, EditMode mode) {
        keyMap.put(key, mode);
    }
    
    public static boolean handleEditModeHotkey(int key, EditButton[] buttons) {
        if (Quest.canQuestsBeEdited()) {
            if (GLFW.GLFW_KEY_0 <= key && key <= GLFW.GLFW_KEY_9) {
                int i = key == GLFW.GLFW_KEY_0 ? 9 : key - GLFW.GLFW_KEY_1;
                if (i < buttons.length) {
                    buttons[i].click();
                    return true;
                }
            } else if (keyMap.containsKey(key)) {
                Collection<EditMode> modes = keyMap.get(key);
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
        for (Map.Entry<Integer, EditMode> entry : keyMap.entries())
            list.add(entry.getKey() + ":" + entry.getValue().name().toLowerCase());
        return list.toArray(new String[0]);
    }
    
    public static void fromConfig(String[] config) {
        clear();
        for (String entry : config) {
            String[] split = entry.split(":");
            if (split.length != 2) continue;
            int key = Integer.parseInt(split[0]);
            EditMode mode = EditMode.valueOf(split[1].toUpperCase());
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
