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
    private int width;
    private float mult = 1F;
    private int maxLength = Integer.MAX_VALUE;
    
    public TextBoxLogic(GuiBase gui, String text, int width) {
        this.gui = gui;
        this.width = width;
        setText(Objects.requireNonNullElse(text, ""));
        
        helper = new TextFieldHelper(this::getText, this::setText, TextFieldHelper.createClipboardGetter(Minecraft.getInstance()),
                TextFieldHelper.createClipboardSetter(Minecraft.getInstance()), this::isTextValid);
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
    
    public void addText(String str) {
        helper.insertText(str);
    }
    
    protected boolean isTextValid(String newText) {
        return newText.length() <= maxLength;
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
    
    public abstract void textChanged();
    
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
        if (getAndClearCursorFlag()) {
            cursorPositionX = (int) (mult * this.gui.getStringWidth(text.substring(0, helper.getCursorPos())));
            cursorPositionY = 0;
        }
    }
    
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
    
    protected boolean isCharacterValid(char c, String rest) {
        return SharedConstants.isAllowedChatCharacter(c);
    }
    
    protected int getCursor() {
        return helper.getCursorPos();
    }
    
    protected boolean getAndClearCursorFlag() {
        if (lastCursor != helper.getCursorPos()) {
            lastCursor = helper.getCursorPos();
            return true;
        }
        return false;
    }
    
    public void resetCursor() {
        helper.setCursorToEnd();
    }
    
    public void setTextAndCursor(String s) {
        setText(s);
        resetCursor();
    }
}