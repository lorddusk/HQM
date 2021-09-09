package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that represents a screen section.
 */
@Environment(EnvType.CLIENT)
public abstract class Graphic {
    
    private final List<LargeButton> buttons = new ArrayList<>();
    private final List<ScrollBar> scrollBars = new ArrayList<>();
    
    public final void drawFull(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        draw(matrices, gui, player, mX, mY);
        drawTooltip(matrices, gui, player, mX, mY);
    }
    
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (LargeButton button : buttons) {
            button.draw(matrices, gui, player, mX, mY);
        }
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(matrices, gui);
        }
    }
    
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, gui, player, mX, mY);
        }
    }
    
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        for (LargeButton button : buttons) {
            if (button.inButtonBounds(gui, mX, mY) && button.isVisible(gui, player) && button.isEnabled(gui, player)) {
                button.onClick(gui, player);
                break;
            }
        }
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onClick(gui, mX, mY);
        }
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
}