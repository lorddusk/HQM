package hardcorequesting.common.client.interfaces.widget;

import net.minecraft.client.gui.GuiGraphics;

public interface Drawable {
    void render(GuiGraphics graphics, int mX, int mY);
    
    default void renderTooltip(GuiGraphics graphics, int mX, int mY) {}
}
