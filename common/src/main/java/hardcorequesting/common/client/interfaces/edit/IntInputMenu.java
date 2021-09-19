package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;

import java.util.UUID;
import java.util.function.IntConsumer;

public class IntInputMenu extends GuiEditMenu {
    
    private final IntConsumer resultConsumer;
    private int amount;
    
    public static void display(GuiQuestBook gui, UUID playerId, String title, int initAmount, IntConsumer resultConsumer) {
        gui.setEditMenu(new IntInputMenu(gui, playerId, title, initAmount, resultConsumer));
    }
    
    public IntInputMenu(GuiQuestBook gui, UUID playerId, String title, int initAmount, IntConsumer resultConsumer) {
        super(gui, playerId, true);
    
        this.resultConsumer = resultConsumer;
        amount = initAmount;
        
        addTextBox(new NumberTextBox(gui, 25, 30, title) {
            @Override
            protected int getValue() {
                return amount;
            }
            
            @Override
            protected void setValue(int number) {
                amount = number;
            }
        });
    }
    
    @Override
    public void save() {
        resultConsumer.accept(amount);
    }
}
