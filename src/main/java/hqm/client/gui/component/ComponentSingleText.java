package hqm.client.gui.component;

import hqm.client.gui.AbstractRender;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class ComponentSingleText extends AbstractRender implements ComponentScrollPane.IScrollRender {

    private final String text;
    private int color = 0x000000, line = 0;
    private float scale = 1.0F, lastScale = 1.0F;
    private final IPage.Side side;
    private final List<String> hoveringText = new ArrayList<>();

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

    public ComponentSingleText setHoveringText(List<String> hov){
        this.hoveringText.clear();
        this.hoveringText.addAll(hov);
        return this;
    }

    public float getScale() {
        return scale;
    }

    public int getWidth(){
        return Math.round(Minecraft.getMinecraft().fontRenderer.getStringWidth(this.text) * this.getScale());
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        if(this.side == side){
            FontRenderer font = gui.mc.fontRenderer;
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.translate(left, top, 0);
            GlStateManager.scale(scale, scale, 1);
            font.drawString(this.text, 0, 0, this.color);
            GlStateManager.popMatrix();
            if(!this.hoveringText.isEmpty()){
                int strWidth = font.getStringWidth(this.text);
                if(mouseX >= left && mouseX <= left + strWidth && mouseY >= top && mouseY <= top + font.FONT_HEIGHT){
                    GlStateManager.pushMatrix();
                    gui.drawHoveringText(this.hoveringText, mouseX, mouseY);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @Override
    public void render(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        this.draw(gui, left, top, width, height, mouseX, mouseY, side);
    }

    @Override
    public void renderRaw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {}

    @Override
    public int getHeight() {
        return Math.round(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * this.scale);
    }
}
