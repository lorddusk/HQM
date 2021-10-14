package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class that represents a screen section.
 * Defines a series of gui functions, and provides some basic widget-managing support.
 */
@Environment(EnvType.CLIENT)
public abstract class Graphic {
    
    private final List<Drawable> drawables = new ArrayList<>();
    private final List<Clickable> clickables = new ArrayList<>();
    private final List<LargeButton> buttons = new ArrayList<>();
    private final List<ScrollBar> scrollBars = new ArrayList<>();
    private final TextBoxGroup textBoxes = new TextBoxGroup();
    private final List<AbstractCheckBox> checkBoxes = new ArrayList<>();
    {
        drawables.add(textBoxes);
        clickables.add(textBoxes);
    }
    
    public final void drawFull(PoseStack matrices, int mX, int mY) {
        draw(matrices, mX, mY);
        drawTooltip(matrices, mX, mY);
    }
    
    public void draw(PoseStack matrices, int mX, int mY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        for (Drawable drawable : drawables) {
            drawable.render(matrices, mX, mY);
        }
    }
    
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, mX, mY);
        }
    }
    
    public void onClick(int mX, int mY, int b) {
        for (Clickable clickable : clickables) {
            if (clickable.onClick(mX, mY))
                return;
        }
    }
    
    public boolean keyPressed(int keyCode) {
        return textBoxes.onKeyStroke(keyCode);
    }
    
    public boolean charTyped(char c) {
        return textBoxes.onCharTyped(c);
    }
    
    public void onDrag(int mX, int mY, int b) {
        for (Clickable clickable : clickables) {
            if (clickable.onDrag(mX, mY))
                return;
        }
    }
    
    public void onRelease(int mX, int mY, int b) {
        for (Clickable clickable : clickables) {
            if (clickable.onRelease(mX, mY))
                return;
        }
    }
    
    public void onScroll(double x, double y, double scroll) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(x, y, scroll);
        }
    }
    
    protected void addButton(LargeButton button) {
        drawables.add(button);
        clickables.add(button);
        buttons.add(button);
    }
    
    protected void addScrollBar(ScrollBar scrollBar) {
        drawables.add(scrollBar);
        clickables.add(scrollBar);
        scrollBars.add(scrollBar);
    }
    
    protected void addTextBox(TextBoxGroup.TextBox box) {
        textBoxes.add(box);
    }
    
    protected void addCheckBox(AbstractCheckBox box) {
        drawables.add(box);
        clickables.add(box);
        checkBoxes.add(box);
    }
    
    protected void reloadTextBoxes() {
        textBoxes.getTextBoxes().forEach(TextBoxGroup.TextBox::reloadText);
    }
}