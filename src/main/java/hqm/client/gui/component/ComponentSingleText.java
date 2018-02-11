package hqm.client.gui.component;

import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class ComponentSingleText implements IRenderer {

    private final String text;
    private int color = 0x000000, line = 0;
    private float scale = 1.0F, lastScale = 1.0F;
    private final IPage.Side side;

    public ComponentSingleText(String text, IPage.Side side) {
        this.text = text;
        this.side = side;
    }

    public static List<ComponentSingleText> from(List<String> lines, int width, IPage.Side side){
        List<ComponentSingleText> list = new ArrayList<>();
        lines.forEach(s -> Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(s, width).forEach(s1 -> list.add(new ComponentSingleText(s1, side))));
        return list;
    }

    public ComponentSingleText setColor(int color) {
        this.color = color;
        return this;
    }

    public ComponentSingleText setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public ComponentSingleText setLastScale(float scale) {
        this.lastScale = scale;
        return this;
    }

    public ComponentSingleText setLine(int line) {
        this.line = line;
        return this;
    }

    public float getScale() {
        return scale;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        if(this.side == side){
            FontRenderer font = gui.mc.fontRenderer;
            GlStateManager.pushMatrix();
            GlStateManager.translate(left, top + this.line * (font.FONT_HEIGHT * this.lastScale), 0);
            GlStateManager.scale(scale, scale, 1);
            font.drawString(this.text, 0, 0, this.color);
            GlStateManager.popMatrix();
        }
    }
}
