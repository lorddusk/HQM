package hardcorequesting.common.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public abstract class TextBoxLogic {
    
    private final TextFieldHelper helper;
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
    
    private void setText(String text) {
        this.text = text;
        textChanged();
    }
    
    public boolean onKeyStroke(int k) {
        return helper.keyPressed(k);
    }
    
    public boolean onCharTyped(char c) {
        if (SharedConstants.isAllowedChatCharacter(c))
            return helper.charTyped(c);
        else return false;
    }
    
    protected int getCursor() {
        return helper.getCursorPos();
    }
    
    protected int getSelectionPos() {
        return helper.getSelectionPos();
    }
    
    protected void setCursor(int cursor) {
        helper.setCursorPos(cursor, Screen.hasShiftDown());
    }
    
    public void resetCursor() {
        helper.setCursorToEnd();
    }
    
    public void setTextAndCursor(String s) {
        setText(s);
        resetCursor();
    }
}