package hardcorequesting.common.client.interfaces.widget;

import hardcorequesting.common.client.interfaces.GuiBase;
import net.minecraft.network.chat.FormattedText;

public class SimpleCheckBox extends AbstractCheckBox {
    
    private boolean value;
    
    public SimpleCheckBox(GuiBase gui, FormattedText label, int x, int y, boolean initialValue) {
        super(gui, label, x, y);
        value = initialValue;
    }
    
    @Override
    public boolean getValue() {
        return value;
    }
    
    @Override
    public void setValue(boolean val) {
        value = val;
    }
}
