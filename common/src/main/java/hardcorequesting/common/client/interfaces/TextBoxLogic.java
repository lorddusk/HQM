package hardcorequesting.common.client.interfaces;

import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class TextBoxLogic {
    
    private static final int TEXT_HEIGHT = 9;
    
    protected final GuiBase gui;
    protected int cursor;
    protected int cursorPositionX;
    protected boolean updatedCursor;
    private String text;
    private List<String> lines;
    private int cursorPositionY;
    private final boolean multiLine;
    private int width;
    private float mult = 1F;
    private int maxLength = Integer.MAX_VALUE;
    private int cursorLine;
    
    public TextBoxLogic(GuiBase gui, String text, int width, boolean multiLine) {
        this.gui = gui;
        this.width = width;
        this.multiLine = multiLine;
        this.text = Objects.requireNonNullElse(text, "");
        textChanged();
        resetCursor();
    }
    
    public int getCursorLine() {
        recalculateCursor();
        return cursorLine;
    }
    
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    public float getMult() {
        return mult;
    }
    
    public void setMult(float mult) {
        this.mult = mult;
    }
    
    @Environment(EnvType.CLIENT)
    public void addText(String str) {
        String newText = text.substring(0, cursor) + str + text.substring(cursor);
        
        newText = getValidText(newText);
        
        if (newText.length() <= maxLength && (multiLine || gui.getStringWidth(newText) * mult <= width)) {
            text = newText;
            moveCursor(str.length());
            textChanged();
        }
    }
    
    private String getValidText(String txt) {
        StringBuilder builder = new StringBuilder();
        for (char c : txt.toCharArray()) {
            if (isCharacterValid(c, builder.toString())) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    @Environment(EnvType.CLIENT)
    private void deleteText(int direction) {
        if (cursor + direction >= 0 && cursor + direction <= text.length()) {
            if (direction > 0) {
                text = text.substring(0, cursor) + text.substring(cursor + 1);
            } else {
                text = text.substring(0, cursor - 1) + text.substring(cursor);
                moveCursor(direction);
            }
            textChanged();
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void moveCursor(int steps) {
        cursor += steps;
        
        updateCursor();
    }
    
    
    public void textChanged() {
        lines = this.gui.getLinesFromText(Translator.plain(text), mult, width).stream().map(Translator::rawString).collect(Collectors.toList());
    }
    
    public List<String> getLines() {
        return lines;
    }
    
    public String getText() {
        return text;
    }
    
    public int getCursorPositionX() {
        recalculateCursor();
        return cursorPositionX;
    }
    
    public int getCursorPositionY() {
        recalculateCursor();
        return cursorPositionY;
    }
    
    public void recalculateCursor() {
        if (updatedCursor) {
            if (multiLine) {
                int tmpCursor = cursor;
                for (int i = 0; i < lines.size(); i++) {
                    if (tmpCursor <= lines.get(i).length()) {
                        cursorPositionX = (int) (mult * this.gui.getStringWidth(lines.get(i).substring(0, tmpCursor)));
                        cursorPositionY = (int) (TEXT_HEIGHT * i * mult);
                        cursorLine = i;
                        break;
                    } else {
                        tmpCursor -= lines.get(i).length();
                    }
                }
            } else {
                cursorPositionX = (int) (mult * this.gui.getStringWidth(text.substring(0, cursor)));
                cursorPositionY = 0;
            }
            
            updatedCursor = false;
        }
    }
    
    public void setText(String text) {
        this.text = getValidText(text);
        textChanged();
    }
    
    @Environment(EnvType.CLIENT)
    public boolean onKeyStroke(int k) {
        if (k == GLFW.GLFW_KEY_LEFT) {
            moveCursor(-1);
            return true;
        } else if (k == GLFW.GLFW_KEY_RIGHT) {
            moveCursor(1);
            return true;
        } else if (k == GLFW.GLFW_KEY_BACKSPACE) {
            deleteText(-1);
            return true;
        } else if (k == GLFW.GLFW_KEY_DELETE) {
            deleteText(1);
            return true;
        } else if (k == GLFW.GLFW_KEY_KP_ENTER || k == GLFW.GLFW_KEY_ENTER) {
            addText("\\n");
            return true;
        } else if (k == GLFW.GLFW_KEY_HOME) {
            cursor = 0;
            updateCursor();
            return true;
        } else if (k == GLFW.GLFW_KEY_END) {
            cursor = text.length();
            updateCursor();
            return true;
        }
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean onCharTyped(char c) {
        if (isCharacterValid(c, getText())) {
            addText(Character.toString(c));
            return true;
        }
        return false;
    }
    
    public void setCursor(int cursor) {
        this.cursor = cursor;
        updateCursor();
    }
    
    protected boolean isCharacterValid(char c, String rest) {
        return SharedConstants.isAllowedChatCharacter(c);
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
    
    public void setTextAndCursor(String s) {
        setText(s);
        resetCursor();
    }
}
