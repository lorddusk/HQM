package hardcorequesting.client.interfaces;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ChatAllowedCharacters;

import java.util.List;

@SideOnly(Side.CLIENT)
public class TextBoxLogic {
    private static final int TEXT_HEIGHT = 9;
    private String text;
    private List<String> lines;
    protected int cursor;
    protected int cursorPositionX;
    private int cursorPositionY;
    private boolean multiLine;
    private int width;
    protected boolean updatedCursor;
    private float mult = 1F;
    private int maxLength = Integer.MAX_VALUE;
    private int cursorLine;

    public int getCursorLine(GuiBase gui) {
        recalculateCursor(gui);
        return cursorLine;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setMult(float mult) {
        this.mult = mult;
    }

    public float getMult() {
        return mult;
    }

    public TextBoxLogic(GuiBase gui, String text, int width, boolean multiLine) {
        this.width = width;
        this.multiLine = multiLine;
        if (text == null) {
            this.text = "";
        } else {
            this.text = text;
        }
        textChanged(gui);
        resetCursor();
    }

    @SideOnly(Side.CLIENT)
    public void addText(GuiBase gui, String str) {
        String newText = text.substring(0, cursor) + str + text.substring(cursor);

        newText = getValidText(newText);

        if (newText.length() <= maxLength && (multiLine || gui.getStringWidth(newText) * mult <= width)) {
            text = newText;
            moveCursor(gui, str.length());
            textChanged(gui);
        }
    }

    private String getValidText(String txt) {
        StringBuilder builder = new StringBuilder();
        for (char c : txt.toCharArray()) {
            if (isCharacterValid(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @SideOnly(Side.CLIENT)
    private void deleteText(GuiBase gui, int direction) {
        if (cursor + direction >= 0 && cursor + direction <= text.length()) {
            if (direction > 0) {
                text = text.substring(0, cursor) + text.substring(cursor + 1);
            } else {
                text = text.substring(0, cursor - 1) + text.substring(cursor);
                moveCursor(gui, direction);
            }
            textChanged(gui);
        }
    }

    @SideOnly(Side.CLIENT)
    private void moveCursor(GuiBase gui, int steps) {
        cursor += steps;

        updateCursor();
    }


    protected void textChanged(GuiBase gui) {
        lines = gui.getLinesFromText(text, mult, width);
    }

    public List<String> getLines() {
        return lines;
    }

    public String getText() {
        return text;
    }

    public int getCursorPositionX(GuiBase gui) {
        recalculateCursor(gui);
        return cursorPositionX;
    }

    public int getCursorPositionY(GuiBase gui) {
        recalculateCursor(gui);
        return cursorPositionY;
    }

    protected void recalculateCursor(GuiBase gui) {
        if (updatedCursor) {
            if (multiLine) {
                int tmpCursor = cursor;
                for (int i = 0; i < lines.size(); i++) {
                    if (tmpCursor <= lines.get(i).length()) {
                        cursorPositionX = (int) (mult * gui.getStringWidth(lines.get(i).substring(0, tmpCursor)));
                        cursorPositionY = (int) (TEXT_HEIGHT * i * mult);
                        cursorLine = i;
                        break;
                    } else {
                        tmpCursor -= lines.get(i).length();
                    }
                }
            } else {
                cursorPositionX = (int) (mult * gui.getStringWidth(text.substring(0, cursor)));
                cursorPositionY = 0;
            }

            updatedCursor = false;
        }
    }

    public void setText(GuiBase gui, String text) {
        this.text = getValidText(text);
        textChanged(gui);
    }

    @SideOnly(Side.CLIENT)
    public void onKeyStroke(GuiBase gui, char c, int k) {
        if (k == 203) {
            moveCursor(gui, -1);
        } else if (k == 205) {
            moveCursor(gui, 1);
        } else if (k == 14) {
            deleteText(gui, -1);
        } else if (k == 211) {
            deleteText(gui, 1);
        } else if (isCharacterValid(c)) {
            addText(gui, Character.toString(c));
        }
    }

    protected boolean isCharacterValid(char c) {
        return ChatAllowedCharacters.isAllowedCharacter(c);
    }

    private void updateCursor() {
        if (cursor < 0) {
            cursor = 0;
        } else if (cursor > text.length()) {
            cursor = text.length();
        }

        updatedCursor = true;
    }

    public void resetCursor() {
        cursor = text.length();
        updatedCursor = true;
    }

    public void setTextAndCursor(GuiBase gui, String s) {
        setText(gui, s);
        resetCursor();
    }
}
