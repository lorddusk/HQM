package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.world.entity.player.Player;

public class GuiEditMenuReputationValue extends GuiEditMenuExtended {
    
    private ReputationMarker marker;
    private int value;
    
    public GuiEditMenuReputationValue(GuiBase gui, Player player, ReputationMarker marker) {
        super(gui, player, true, -1, -1, 25, 30);
        
        this.marker = marker;
        this.value = marker.getValue();
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.repValue.tierValue") {
            @Override
            protected boolean isNegativeAllowed() {
                return true;
            }
            
            @Override
            protected int getValue() {
                return value;
            }
            
            @Override
            protected void setValue(int number) {
                value = number;
            }
        });
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
    
    @Override
    public void save(GuiBase gui) {
        marker.setValue(value);
        GuiQuestBook.selectedReputation.sort();
        SaveHelper.add(SaveHelper.EditType.REPUTATION_MARKER_CHANGE);
    }
}
