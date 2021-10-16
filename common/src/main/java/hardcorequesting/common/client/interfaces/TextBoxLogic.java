package hardcorequesting.common.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class TextBoxLogic {
    
    private final TextFieldHelper helper;
    protected final GuiBase gui;
    protected int cursorPositionX, cursorPositionY;
    private int lastCursor;
    private String text;
    private float mult = 1F;
    private final int maxLength;
    
    public TextBoxLogic(GuiBase gui, String text, int charLimit) {
        this.gui = gui;
        maxLength = charLimit;
        setText(Objects.requireNonNullElse(text, ""));
        
        helper = new TextFieldHelper(this::getText, this::setText, TextFieldHelper.createClipboardGetter(Minecraft.getInstance()),
                TextFieldHelper.createClipboardSetter(Minecraft.getInstance()), this::isTextValid);
    }
    
    public float getMult() {
        return mult;
    }
    
    public void setMult(float mult) {
        this.mult = mult;
    }
    
    public void addText(String str) {
        helper.insertText(str);
    }
    
    protected boolean isTextValid(String newText) {
        return newText.length() <= maxLength;
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
    
    public abstract void textChanged();
    
    public String getText() {
        return text;
    }
    
    public int getCursorPositionX() {
        checkCursor();
        return cursorPositionX;
    }
    
    public int getCursorPositionY() {
        checkCursor();
        return cursorPositionY;
    }
    
    public void checkCursor() {
        if (lastCursor != helper.getCursorPos()) {
            recalculateCursorDetails(lastCursor = helper.getCursorPos());
        }
    }
    
    protected abstract void recalculateCursorDetails(int cursor);
    
    public void setText(String text) {
        this.text = getValidText(text);
        textChanged();
    }
    
    public boolean onKeyStroke(int k) {
        if (k == GLFW.GLFW_KEY_LEFT) {
            helper.moveByChars(-1);
            return true;
        } else if (k == GLFW.GLFW_KEY_RIGHT) {
            helper.moveByChars(1);
            return true;
        } else if (k == GLFW.GLFW_KEY_BACKSPACE) {
            helper.removeCharsFromCursor(-1);
            return true;
        } else if (k == GLFW.GLFW_KEY_DELETE) {
            helper.removeCharsFromCursor(1);
            return true;
        } else if (k == GLFW.GLFW_KEY_HOME) {
            helper.setCursorToStart();
            return true;
        } else if (k == GLFW.GLFW_KEY_END) {
            helper.setCursorToEnd();
            return true;
        }
        return false;
    }
    
    public boolean onCharTyped(char c) {
        return helper.charTyped(c);
    }
    
    protected boolean isCharacterValid(char c) {
        return SharedConstants.isAllowedChatCharacter(c);
    }
    
    protected int getCursor() {
        return helper.getCursorPos();
    }
    
    public void resetCursor() {
        helper.setCursorToEnd();
    }
    
    public void setTextAndCursor(String s) {
        setText(s);
        resetCursor();
    }
}