package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.graphic.EditReputationGraphic;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;

import java.util.UUID;

public class GuiEditMenuReputationValue extends GuiEditMenuExtended {
    
    private ReputationMarker marker;
    private int value;
    
    public GuiEditMenuReputationValue(GuiBase gui, UUID playerId, ReputationMarker marker) {
        super(gui, playerId, true, -1, -1);
        
        this.marker = marker;
        this.value = marker.getValue();
        
        textBoxes.add(new NumberTextBox(gui, 25, 30, "hqm.repValue.tierValue") {
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
    public void save() {
        marker.setValue(value);
        EditReputationGraphic.selectedReputation.sort();
        SaveHelper.add(EditType.REPUTATION_MARKER_CHANGE);
    }
}
