package hardcorequesting.client;

import hardcorequesting.client.interfaces.GuiQuestBook;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class KeyboardHandler {

    private static Map<Integer, EditMode> keyMap = new HashMap<>(EditMode.values().length);
    static {
        keyMap.put(Keyboard.KEY_M, EditMode.MOVE);
        keyMap.put(Keyboard.KEY_DELETE, EditMode.DELETE);
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
            EditMode mode = keyMap.get(key);
            for (EditButton button : buttons)
            {
                if (button.matchesMode(mode))
                {
                    button.click();
                    return true;
                }
            }
        }
        return false;
    }
}
