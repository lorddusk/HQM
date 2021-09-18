package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.util.Translator;

import java.util.UUID;

public abstract class GuiEditMenuExtended extends GuiEditMenu {
    
    private static final int ARROW_SRC_X = 244;
    private static final int ARROW_SRC_Y = 176;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 10;
    protected static final int BOX_OFFSET = 30;
    private final int ARROW_X_LEFT;
    private final int ARROW_Y;
    private final int ARROW_DESCRIPTION_Y;
    private final int ARROW_X_RIGHT;
    protected TextBoxGroup textBoxes;
    private boolean clicked;
    
    protected GuiEditMenuExtended(UUID playerId, boolean isControlOnFirstPage, int arrowX, int arrowY) {
        super(playerId, isControlOnFirstPage);
        
        this.textBoxes = new TextBoxGroup();
        ARROW_X_LEFT = arrowX;
        ARROW_Y = arrowY;
        ARROW_DESCRIPTION_Y = ARROW_Y + 20;
        ARROW_X_RIGHT = ARROW_X_LEFT + 130;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (isArrowVisible()) {
            drawArrow(matrices, gui, mX, mY, true);
            drawArrow(matrices, gui, mX, mY, false);
            
            gui.drawCenteredString(matrices, Translator.plain(getArrowText()), ARROW_X_LEFT + ARROW_W, ARROW_Y, 0.7F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
            String description = getArrowDescription();
            if (description != null) {
                gui.drawString(matrices, gui.getLinesFromText(Translator.plain(description), 0.7F, ARROW_X_RIGHT - ARROW_X_LEFT + ARROW_W), ARROW_X_LEFT, ARROW_DESCRIPTION_Y, 0.7F, 0x404040);
            }
        }
        
        textBoxes.draw(matrices, gui);
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        if (isArrowVisible()) {
            if (inArrowBounds(gui, mX, mY, true)) {
                onArrowClick(true);
                clicked = true;
            } else if (inArrowBounds(gui, mX, mY, false)) {
                onArrowClick(false);
                clicked = true;
            }
        }
        
        textBoxes.onClick(gui, mX, mY);
    }
    
    @Override
    public void onKeyStroke(GuiBase gui, char c, int k) {
        super.onKeyStroke(gui, c, k);
    
        if (k == -1)
            textBoxes.onCharTyped(gui, c);
        else
            textBoxes.onKeyStroke(gui, k);
    }
    
    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);
        
        clicked = false;
    }
    
    protected boolean isArrowVisible() {
        return ARROW_Y != -1;
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
    
    protected abstract String getArrowText();
    
    protected abstract String getArrowDescription();
    
}
