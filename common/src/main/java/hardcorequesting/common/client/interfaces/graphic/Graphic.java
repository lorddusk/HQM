package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that represents a screen section.
 */
@Environment(EnvType.CLIENT)
public abstract class Graphic {
    
    private final List<LargeButton> buttons = new ArrayList<>();
    private final List<ScrollBar> scrollBars = new ArrayList<>();
    private final TextBoxGroup textBoxes = new TextBoxGroup();
    
    public final void drawFull(PoseStack matrices, int mX, int mY) {
        draw(matrices, mX, mY);
        drawTooltip(matrices, mX, mY);
    }
    
    public void draw(PoseStack matrices, int mX, int mY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (LargeButton button : buttons) {
            button.draw(matrices, mX, mY);
        }
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(matrices);
        }
        
        textBoxes.draw(matrices);
    }
    
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, mX, mY);
        }
    }
    
    public void onClick(int mX, int mY, int b) {
        for (LargeButton button : buttons) {
            if (button.inButtonBounds(mX, mY) && button.isVisible() && button.isEnabled()) {
                button.onClick();
                break;
            }
        }
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onClick(mX, mY);
        }
        
        textBoxes.onClick(mX, mY);
    }
    
    public boolean keyPressed(int keyCode) {
        return textBoxes.onKeyStroke(keyCode);
    }
    
    public boolean charTyped(char c) {
        return textBoxes.onCharTyped(c);
    }
    
    public void onDrag(int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onDrag(mX, mY);
        }
    }
    
    public void onRelease(int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onRelease(mX, mY);
        }
    }
    
    public void onScroll(double x, double y, double scroll) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(x, y, scroll);
        }
    }
    
    protected void addButton(LargeButton button) {
        buttons.add(button);
    }
    
    protected void addScrollBar(ScrollBar scrollBar) {
        scrollBars.add(scrollBar);
    }
    
    protected void addTextBox(TextBoxGroup.TextBox box) {
        textBoxes.add(box);
    }
    
    protected void reloadTextBoxes() {
        textBoxes.getTextBoxes().forEach(TextBoxGroup.TextBox::reloadText);
    }
}