package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

public abstract class AbstractCheckBox {
    private static final int CHECK_BOX_SRC_X = 192;
    private static final int CHECK_BOX_SRC_Y = 102;
    private static final int CHECK_BOX_SIZE = 7;
    
    private int x;
    private int y;
    private String name;
    private List<FormattedText> cached;
    private int width = Integer.MAX_VALUE;
    
    protected AbstractCheckBox(String name, int x, int y) {
        this.x = x;
        this.y = y;
        this.name = name;
    }
    
    protected AbstractCheckBox(String name, int x, int y, int width) {
        this(name, x, y);
        this.width = width;
    }
    
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        if (!isVisible()) {
            return;
        }
        
        if (cached == null) {
            cached = gui.getLinesFromText(Translator.translatable(name), 0.7F, width);
        }
        
        boolean selected = getValue();
        boolean hover = gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY);
        
        gui.applyColor(0xFFFFFFFF);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        gui.drawRect(matrices, x, y, CHECK_BOX_SRC_X + (selected ? CHECK_BOX_SIZE : 0), CHECK_BOX_SRC_Y + (hover ? CHECK_BOX_SIZE : 0), CHECK_BOX_SIZE, CHECK_BOX_SIZE);
        gui.drawString(matrices, cached, x + 12, y + 2, 0.7F, 0x404040);
    }
    
    public void onClick(GuiBase gui, int mX, int mY) {
        if (isVisible() && gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY)) {
            setValue(!getValue());
        }
    }
    
    protected boolean isVisible() {
        return true;
    }
    
    public abstract boolean getValue();
    
    public abstract void setValue(boolean val);
}
