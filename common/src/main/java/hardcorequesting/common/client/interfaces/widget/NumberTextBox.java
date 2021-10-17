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
    private final boolean allowNegative;
    
    public NumberTextBox(GuiBase gui, int x, int y, FormattedText title, IntSupplier getter, IntConsumer setter) {
        this(gui, x, y, title, false, getter, setter);
    }
    
    public NumberTextBox(GuiBase gui, int x, int y, FormattedText title, boolean allowNegative, IntSupplier getter, IntConsumer setter) {
        this(gui, x, y, title, allowNegative, 32, getter, setter);
    }
    
    public NumberTextBox(GuiBase gui, int x, int y, FormattedText title, boolean allowNegative, int charLimit, IntSupplier getter, IntConsumer setter) {
        super(gui, "", x, y, false, charLimit);
        this.title = title;
        this.getter = getter;
        this.setter = setter;
        this.allowNegative = allowNegative;
        reloadText();
    }
    
    @Override
    protected String getValidText(String txt) {
        return super.getValidText(txt).replaceAll(allowNegative ? "[^0-9-]" : "[^0-9]", "");
    }
    
    @Override
    public boolean onCharTyped(char c) {
        if (Character.isDigit(c) || allowNegative && c == '-')
            return super.onCharTyped(c);
        else return false;
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