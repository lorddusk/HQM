package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import net.minecraft.world.entity.player.Player;

import java.util.function.IntConsumer;

public class SetDeathMenu extends GuiEditMenuExtended {
    
    private final IntConsumer resultConsumer;
    private int deaths;
    
    public static void display(GuiBase gui, Player player, int initDeaths, IntConsumer resultConsumer) {
        gui.setEditMenu(new SetDeathMenu(gui, player, initDeaths, resultConsumer));
    }
    
    public SetDeathMenu(GuiBase gui, Player player, int initDeaths, IntConsumer resultConsumer) {
        super(gui, player, true, -1, -1, 25, 30);
        
        this.resultConsumer = resultConsumer;
        deaths = initDeaths;
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.deathTask.reqDeathCount") {
            @Override
            protected int getValue() {
                return deaths;
            }
            
            @Override
            protected void setValue(int number) {
                deaths = number;
            }
        });
    }
    
    @Override
    public void save(GuiBase gui) {
        resultConsumer.accept(deaths);
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
