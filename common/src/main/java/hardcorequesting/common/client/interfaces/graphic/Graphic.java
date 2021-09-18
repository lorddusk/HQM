package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
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
    
    public final void drawFull(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        draw(matrices, gui, mX, mY);
        drawTooltip(matrices, gui, mX, mY);
    }
    
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (LargeButton button : buttons) {
            button.draw(matrices, gui, mX, mY);
        }
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(matrices, gui);
        }
        
        textBoxes.draw(matrices, gui);
    }
    
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, gui, mX, mY);
        }
    }
    
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        for (LargeButton button : buttons) {
            if (button.inButtonBounds(gui, mX, mY) && button.isVisible(gui) && button.isEnabled(gui)) {
                button.onClick(gui);
                break;
            }
        }
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onClick(gui, mX, mY);
        }
        
        textBoxes.onClick(gui, mX, mY);
    }
    
    public boolean keyPressed(GuiQuestBook gui, int keyCode) {
        return textBoxes.onKeyStroke(gui, keyCode);
    }
    
    public boolean charTyped(GuiQuestBook gui, char c) {
        return textBoxes.onCharTyped(gui, c);
    }
    
    public void onDrag(GuiQuestBook gui, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onDrag(gui, mX, mY);
        }
    }
    
    public void onRelease(GuiQuestBook gui, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onRelease(gui, mX, mY);
        }
    }
    
    public void onScroll(GuiQuestBook gui, double x, double y, double scroll) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(gui, x, y, scroll);
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
}