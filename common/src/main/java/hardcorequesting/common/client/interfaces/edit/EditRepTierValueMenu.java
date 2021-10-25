package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.util.Translator;

import java.util.function.Consumer;

public class EditRepTierValueMenu extends GuiEditMenu {
    
    private final Consumer<Integer> resultConsumer;
    private int value;
    
    public static void display(GuiQuestBook gui, int valueIn, Consumer<Integer> resultConsumer) {
        gui.setEditMenu(new EditRepTierValueMenu(gui, valueIn, resultConsumer));
    }
    
    private EditRepTierValueMenu(GuiQuestBook gui, int valueIn, Consumer<Integer> resultConsumer) {
        super(gui, true);
    
        this.resultConsumer = resultConsumer;
        this.value = valueIn;
        
        addTextBox(new NumberTextBox(gui, 25, 30, Translator.translatable("hqm.repValue.tierValue"), true, () -> value, value1 -> value = value1));
    }
    
    @Override
    public void save() {
        resultConsumer.accept(value);
    }
}