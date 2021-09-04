package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import net.minecraft.world.entity.player.Player;

import java.util.function.IntConsumer;

public class IntInputMenu extends GuiEditMenuExtended {
    
    private final IntConsumer resultConsumer;
    private int amount;
    
    public static void display(GuiBase gui, Player player, String title, int initAmount, IntConsumer resultConsumer) {
        gui.setEditMenu(new IntInputMenu(gui, player, title, initAmount, resultConsumer));
    }
    
    public IntInputMenu(GuiBase gui, Player player, String title, int initAmount, IntConsumer resultConsumer) {
        super(gui, player, true, -1, -1);
        
        this.resultConsumer = resultConsumer;
        amount = initAmount;
        
        textBoxes.add(new NumberTextBox(gui, 25, 30, title) {
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
    public void save(GuiBase gui) {
        resultConsumer.accept(amount);
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        
    }
    
    @Override
    protected String getArrowText() {
        return null;
    }
    
    @Override
    protected String getArrowDescription() {
        return null;
    }
}
