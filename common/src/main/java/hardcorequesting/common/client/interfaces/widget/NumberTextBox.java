package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.util.Translator;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class NumberTextBox extends TextBoxGroup.TextBox {
    
    public static final int TEXT_OFFSET = -10;
    private final String title;
    private final boolean loaded;
    private final IntSupplier getter;
    private final IntConsumer setter;
    
    public NumberTextBox(GuiBase gui, int x, int y, String title, IntSupplier getter, IntConsumer setter) {
        super(gui, "", x, y, false, 32);
        loaded = true;
        reloadText();
        this.title = title;
        this.getter = getter;
        this.setter = setter;
    }
    
    @Override
    protected boolean isCharacterValid(char c) {
        return Character.isDigit(c);
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
                if (getText().isEmpty()) {
                    number = 1;
                } else {
                    number = Integer.parseInt(getText());
                }
                setter.accept(number);
            } catch (Exception ignored) {
            }
        }
    }
    
    @Override
    public void reloadText() {
        setTextAndCursor(isVisible() ? String.valueOf(getter.getAsInt()) : "0");
    }
}
