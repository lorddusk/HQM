package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;

import java.util.function.IntConsumer;

public class IntInputMenu extends GuiEditMenu {
    
    private final IntConsumer resultConsumer;
    private int amount;
    
    public static void display(GuiQuestBook gui, String title, int initAmount, IntConsumer resultConsumer) {
        gui.setEditMenu(new IntInputMenu(gui, title, initAmount, resultConsumer));
    }
    
    public IntInputMenu(GuiQuestBook gui, String title, int initAmount, IntConsumer resultConsumer) {
        super(gui, true);
    
        this.resultConsumer = resultConsumer;
        amount = initAmount;
        
        addTextBox(new NumberTextBox(gui, 25, 30, title, () -> amount, value -> amount = value));
    }
    
    @Override
    public void save() {
        resultConsumer.accept(amount);
    }
}
