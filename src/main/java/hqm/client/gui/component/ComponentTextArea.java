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
public class ComponentTextArea implements IRenderer {

    private final IPage.Side side;
    private final List<ComponentSingleText> text;

    public ComponentTextArea(List<ComponentSingleText> text, IPage.Side side){
        this.side = side;
        this.text = text;
    }

    public ComponentTextArea(List<String> lines, int width, IPage.Side side){
        this.side = side;
        List<ComponentSingleText> list = new ArrayList<>();
        lines.forEach(s -> Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(s, width).forEach(s1 -> list.add(new ComponentSingleText(s1, side))));
        this.text = list;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        if(side == this.side){
            List<ComponentSingleText> text1 = this.text;
            float lastScale = 1.0F;
            for (int i = 0; i < text1.size(); i++) {
                ComponentSingleText text = text1.get(i);
                text.setLine(i);
                text.setLastScale(lastScale);
                text.draw(gui, left, top, width, height, mouseX, mouseY, side);
                lastScale = text.getScale();
            }
        }
    }

}
