package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.util.Translator;

public abstract class NumberTextBox extends TextBoxGroup.TextBox {
    
    public static final int TEXT_OFFSET = -10;
    private String title;
    private boolean loaded;
    
    public NumberTextBox(GuiBase gui, int x, int y, String title) {
        super(gui, "", x, y, false);
        loaded = true;
        reloadText();
        this.title = title;
    }
    
    @Override
    protected boolean isCharacterValid(char c, String rest) {
        return rest.length() < 32 && (Character.isDigit(c) || (c == '-' && isNegativeAllowed()));
    }
    
    protected boolean isNegativeAllowed() {
        return false;
    }
    
    @Override
    protected void draw(PoseStack matrices, boolean selected) {
        super.draw(matrices, selected);
        
        this.gui.drawString(matrices, Translator.translatable(title), x, y + TEXT_OFFSET, 0x404040);
    }
    
    @Override
    public void textChanged() {
        if (loaded) {
            try {
                int number;
                if (getText().equals("")) {
                    number = 1;
                } else {
                    number = Integer.parseInt(getText());
                }
                setValue(number);
            } catch (Exception ignored) {
            }
        }
    }
    
    @Override
    public void reloadText() {
        setTextAndCursor(isVisible() ? String.valueOf(getValue()) : "0");
    }
    
    protected abstract int getValue();
    
    protected abstract void setValue(int number);
}
