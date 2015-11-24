package hardcorequesting.client;

import hardcorequesting.client.interfaces.GuiQuestBook;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyboardHandler {

    private static Map<Integer, Set<EditMode>> keyMap;
    static {
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

    private static void addKeymap(int key, EditMode mode)
    {
        if (keyMap == null) keyMap = new HashMap<>();
        Set<EditMode> set = keyMap.get(key);
        if (set == null) set = new HashSet<>();
        set.add(mode);
        keyMap.put(key, set);
    }

    public static boolean pressedHotkey(GuiQuestBook gui, int key, EditButton[] buttons)
    {
        if (key == Keyboard.KEY_BACK) {
            gui.goBack();
            return true;
        } else if (key >= Keyboard.KEY_1 && key <= Keyboard.KEY_0)
        {
            int i = key - Keyboard.KEY_1;
            if (i < buttons.length) {
                buttons[i].click();
                return true;
            }
        } else if (keyMap.containsKey(key)) {
            Set<EditMode> modes = keyMap.get(key);
            for (EditButton button : buttons)
            {
                for (EditMode mode : modes) {
                    if (button.matchesMode(mode)) {
                        button.click();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
