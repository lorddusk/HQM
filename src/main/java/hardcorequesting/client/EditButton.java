package hardcorequesting.client;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;

import java.util.List;

public class EditButton {

    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_ICON_SIZE = 12;
    private static final int BUTTON_ICON_SRC_X = 0;
    private static final int BUTTON_ICON_SRC_Y = 0;
    private static final int EDIT_BUTTONS_PER_ROW = 2;
    private static final int EDIT_BUTTONS_SRC_PER_ROW = 8;

    private GuiQuestBook guiQuestBook;
    private int x;
    private int y;
    private EditMode mode;
    private List<String> text;

    public static EditButton[] createButtons(GuiQuestBook gui, EditMode... modes) {
        EditButton[] ret = new EditButton[modes.length];
        for (int i = 0; i < modes.length; i++) {
            EditMode mode = modes[i];
            ret[i] = new EditButton(gui, mode, i);
        }
        return ret;
    }

    public EditButton(GuiQuestBook guiQuestBook, EditMode mode, int id) {
        this.guiQuestBook = guiQuestBook;
        this.mode = mode;

        int x = id % EDIT_BUTTONS_PER_ROW;
        int y = id / EDIT_BUTTONS_PER_ROW;

        this.x = -38 + x * 20;
        this.y = 5 + y * 20;
    }

    public void draw(int mX, int mY) {
        int srcY = guiQuestBook.getCurrentMode() == mode ? 2 : guiQuestBook.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY) ? 1 : 0;
        guiQuestBook.drawRect(x, y, 256 - BUTTON_SIZE, srcY * BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        guiQuestBook.drawRect(x + 2, y + 2,
                BUTTON_ICON_SRC_X + (mode.ordinal() % EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE,
                BUTTON_ICON_SRC_Y + (mode.ordinal() / EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE,
                BUTTON_ICON_SIZE, BUTTON_ICON_SIZE);
    }

    public void drawInfo(int mX, int mY) {
        if (guiQuestBook.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
            if (text == null) {
                text = guiQuestBook.getLinesFromText(mode.getName() + "\n\n" + mode.getDescription(), 1F, 150);
                for (int i = 1; i < text.size(); i++) {
                    text.set(i, GuiColor.GRAY + text.get(i));
                }
            }

            guiQuestBook.drawMouseOver(text, mX + guiQuestBook.getLeft(), mY + guiQuestBook.getTop());
        }
    }

    public boolean onClick(int mX, int mY) {
        if (guiQuestBook.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
            guiQuestBook.setCurrentMode(mode);
            guiQuestBook.modifyingQuest = null;
            guiQuestBook.modifyingBar = null;
            return true;
        }

        return false;
    }

    public boolean click() {
        return onClick(x, y);
    }

    public boolean matchesMode(EditMode mode) {
        return this.mode == mode;
    }
}
