package hardcorequesting.common.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public abstract class TextBoxLogic {
    
    private final TextFieldHelper helper;
    private int lastCursor;
    private String text;
    private final int maxLength;
    
    public TextBoxLogic(String text, int charLimit) {
        maxLength = charLimit;
        this.text = text;
        
        helper = new TextFieldHelper(this::getText, this::setText, this::getStrippedClipboard,
                TextFieldHelper.createClipboardSetter(Minecraft.getInstance()), this::isTextValid);
    }
    
    protected String getStrippedClipboard() {
        return TextFieldHelper.getClipboardContents(Minecraft.getInstance());
    }
    
    public void addText(String str) {
        helper.insertText(str);
    }
    
    protected boolean isTextValid(String newText) {
        return newText.length() <= maxLength;
    }
    
    public abstract void textChanged();
    
    public String getText() {
        return text;
    }
    
    public void checkCursor() {
        if (lastCursor != helper.getCursorPos()) {
            recalculateCursorDetails(lastCursor = helper.getCursorPos());
        }
    }
    
    protected abstract void recalculateCursorDetails(int cursor);
    
    private void setText(String text) {
        this.text = text;
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
        if (SharedConstants.isAllowedChatCharacter(c))
            return helper.charTyped(c);
        else return false;
    }
    
    protected int getCursor() {
        return helper.getCursorPos();
    }
    
    protected void setCursor(int cursor) {
        helper.setCursorPos(cursor, false);
    }
    
    public void resetCursor() {
        helper.setCursorToEnd();
    }
    
    public void setTextAndCursor(String s) {
        setText(s);
        resetCursor();
    }
}