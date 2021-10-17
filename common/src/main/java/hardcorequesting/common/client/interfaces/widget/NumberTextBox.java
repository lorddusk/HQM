package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import net.minecraft.network.chat.FormattedText;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class NumberTextBox extends TextBoxGroup.TextBox {
    
    public static final int TEXT_OFFSET = -10;
    private final FormattedText title;
    private final IntSupplier getter;
    private final IntConsumer setter;
    
    public NumberTextBox(GuiBase gui, int x, int y, FormattedText title, IntSupplier getter, IntConsumer setter) {
        this(gui, x, y, title, getter, setter, 32);
    }
    
    public NumberTextBox(GuiBase gui, int x, int y, FormattedText title, IntSupplier getter, IntConsumer setter, int charLimit) {
        super(gui, "", x, y, false, charLimit);
        this.title = title;
        this.getter = getter;
        this.setter = setter;
        reloadText();
    }
    
    @Override
    protected boolean isCharacterValid(char c) {
        return Character.isDigit(c);
    }
    
    @Override
    protected void draw(PoseStack matrices, boolean selected) {
        super.draw(matrices, selected);
        
        this.gui.drawString(matrices, title, x, y + TEXT_OFFSET, 0x404040);
    }
    
    @Override
    public void textChanged() {
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
    
    @Override
    public void reloadText() {
        setTextAndCursor(isVisible() ? String.valueOf(getter.getAsInt()) : "0");
    }
}
