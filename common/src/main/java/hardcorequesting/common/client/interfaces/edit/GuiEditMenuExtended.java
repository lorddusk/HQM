package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;

import java.util.UUID;

public abstract class GuiEditMenuExtended extends GuiEditMenu {
    
    protected static final int BOX_OFFSET = 30;
    protected TextBoxGroup textBoxes;
    
    protected GuiEditMenuExtended(GuiBase gui, UUID playerId, boolean isControlOnFirstPage) {
        super(gui, playerId, isControlOnFirstPage);
        
        this.textBoxes = new TextBoxGroup();
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        textBoxes.draw(matrices);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        textBoxes.onClick(mX, mY);
    }
    
    @Override
    public void onKeyStroke(char c, int k) {
        super.onKeyStroke(c, k);
    
        if (k == -1)
            textBoxes.onCharTyped(c);
        else
            textBoxes.onKeyStroke(k);
    }
}