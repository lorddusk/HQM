package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import net.minecraft.network.chat.FormattedText;

public abstract class ArrowSelectionHelper {
    private static final int ARROW_SRC_X = 244;
    private static final int ARROW_SRC_Y = 176;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 10;
    private final int ARROW_X_LEFT;
    private final int ARROW_Y;
    private final int ARROW_DESCRIPTION_Y;
    private final int ARROW_X_RIGHT;
    
    private final GuiBase gui;
    private boolean clicked;
    
    public ArrowSelectionHelper(GuiBase gui, int arrowX, int arrowY) {
        this.gui = gui;
        
        ARROW_X_LEFT = arrowX;
        ARROW_Y = arrowY;
        ARROW_DESCRIPTION_Y = ARROW_Y + 20;
        ARROW_X_RIGHT = ARROW_X_LEFT + 130;
    }
    
    public void render(PoseStack matrices, int mX, int mY) {
        if (isArrowVisible()) {
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            
            drawArrow(matrices, gui, mX, mY, true);
            drawArrow(matrices, gui, mX, mY, false);
            
            gui.drawCenteredString(matrices, getArrowText(), ARROW_X_LEFT + ARROW_W, ARROW_Y, 0.7F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
            FormattedText description = getArrowDescription();
            if (description != null) {
                gui.drawString(matrices, gui.getLinesFromText(description, 0.7F, ARROW_X_RIGHT - ARROW_X_LEFT + ARROW_W), ARROW_X_LEFT, ARROW_DESCRIPTION_Y, 0.7F, 0x404040);
            }
        }
    }
    
    public void onClick(int mX, int mY) {
        if (isArrowVisible()) {
            if (inArrowBounds(gui, mX, mY, true)) {
                onArrowClick(true);
                clicked = true;
            } else if (inArrowBounds(gui, mX, mY, false)) {
                onArrowClick(false);
                clicked = true;
            }
        }
    }
    
    public void onRelease() {
        clicked = false;
    }
    
    protected boolean isArrowVisible() {
        return true;
    }
    
    private void drawArrow(PoseStack matrices, GuiBase gui, int mX, int mY, boolean left) {
        int srcX = ARROW_SRC_X + (left ? 0 : ARROW_W);
        int srcY = ARROW_SRC_Y + (inArrowBounds(gui, mX, mY, left) ? clicked ? 1 : 2 : 0) * ARROW_H;
        
        gui.drawRect(matrices, left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, srcX, srcY, ARROW_W, ARROW_H);
    }
    
    private boolean inArrowBounds(GuiBase gui, int mX, int mY, boolean left) {
        return gui.inBounds(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, ARROW_W, ARROW_H, mX, mY);
    }
    
    protected abstract void onArrowClick(boolean left);
    
    protected abstract FormattedText getArrowText();
    
    protected abstract FormattedText getArrowDescription();
}
