package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import net.minecraft.world.entity.player.Player;

import java.util.function.IntConsumer;

public class ReputationKillsMenu extends GuiEditMenuExtended {
    
    private final IntConsumer resultConsumer;
    private int kills;
    
    public static void display(GuiBase gui, Player player, int initKills, IntConsumer resultConsumer) {
        gui.setEditMenu(new ReputationKillsMenu(gui, player, initKills, resultConsumer));
    }
    
    public ReputationKillsMenu(GuiBase gui, Player player, int initKills, IntConsumer resultConsumer) {
        super(gui, player, true, -1, -1, 25, 30);
        
        this.resultConsumer = resultConsumer;
        kills = initKills;
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.mobTask.reqKills") {
            @Override
            protected int getValue() {
                return kills;
            }
            
            @Override
            protected void setValue(int number) {
                kills = number;
            }
        });
    }
    
    @Override
    public void save(GuiBase gui) {
        resultConsumer.accept(kills);
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
