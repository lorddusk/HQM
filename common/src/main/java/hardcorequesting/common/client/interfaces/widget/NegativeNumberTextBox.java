package hardcorequesting.common.client.interfaces.widget;

import hardcorequesting.common.client.interfaces.GuiQuestBook;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class NegativeNumberTextBox extends NumberTextBox {
    public NegativeNumberTextBox(GuiQuestBook gui, int x, int y, String title, IntSupplier getter, IntConsumer setter) {
        super(gui, x, y, title, getter, setter);
    }
    
    @Override
    protected boolean isCharacterValid(char c) {
        return super.isCharacterValid(c) || c == '-';
    }
}
