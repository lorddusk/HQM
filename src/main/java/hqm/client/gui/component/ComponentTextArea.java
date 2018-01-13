package hqm.client.gui.component;

import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class ComponentTextArea implements IRenderer {

    private IPage.Side side;
    private List<String> text = new ArrayList<>();
    private int defaultTextColor = 0x000000;

    public ComponentTextArea(IPage.Side side, List<String> text){
        this.side = side;
        this.text.addAll(text);
    }

    public ComponentTextArea setDefaultTextColor(int color){
        this.defaultTextColor = defaultTextColor;
        return this;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        if(side == this.side){
            FontRenderer font = gui.mc.fontRenderer;
            List<String> realLines = new ArrayList<>();
            this.text.forEach(s -> realLines.addAll(font.listFormattedStringToWidth(s, width)));
            for(int i = 0; i < realLines.size(); i++){
                GlStateManager.pushMatrix();
                GlStateManager.translate(left, (int) (top + (i * font.FONT_HEIGHT * 0.9F)), 0);
                GlStateManager.scale(0.9F, 0.9F, 1);
                font.drawString(realLines.get(i), 0, 0, this.defaultTextColor);
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {

    }

    @Override
    public void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {

    }

    @Override
    public void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, IPage.Side side) {

    }

    @Override
    public void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, IPage.Side side) {

    }

}
