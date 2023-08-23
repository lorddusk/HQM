package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public abstract class AbstractCheckBox implements Drawable, Clickable {
    private static final int CHECK_BOX_SRC_X = 192;
    private static final int CHECK_BOX_SRC_Y = 102;
    private static final int CHECK_BOX_SIZE = 7;
    
    private final int x;
    private final int y;
    private final GuiBase gui;
    private final List<FormattedText> cached;
    
    protected AbstractCheckBox(GuiBase gui, FormattedText label, int x, int y) {
        this(gui, label, x, y, Integer.MAX_VALUE);
    }
    
    protected AbstractCheckBox(GuiBase gui, FormattedText label, int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.gui = gui;
        cached = gui.getLinesFromText(label, 0.7F, width);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mX, int mY) {
        if (!isVisible()) {
            return;
        }
        
        boolean selected = getValue();
        boolean hover = gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY);
        
        gui.applyColor(0xFFFFFFFF);

        gui.drawRect(graphics, GuiBase.MAP_TEXTURE, x, y, CHECK_BOX_SRC_X + (selected ? CHECK_BOX_SIZE : 0), CHECK_BOX_SRC_Y + (hover ? CHECK_BOX_SIZE : 0), CHECK_BOX_SIZE, CHECK_BOX_SIZE);
        gui.drawString(graphics, cached, x + 12, y + 2, 0.7F, 0x404040);
    }
    
    @Override
    public boolean onClick(int mX, int mY) {
        if (isVisible() && gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY)) {
            setValue(!getValue());
            return true;
        }
        return false;
    }
    
    protected boolean isVisible() {
        return true;
    }
    
    public abstract boolean getValue();
    
    public abstract void setValue(boolean val);
}
