package hqm.client.gui.component;

import hqm.client.gui.AbstractRender;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
public class ComponentTextArea extends AbstractRender {

    private final IPage.Side side;
    private final List<ComponentSingleText> text;

    public ComponentTextArea(List<ComponentSingleText> text, IPage.Side side){
        this.side = side;
        this.text = text;
    }

    public ComponentTextArea(List<String> lines, int width, IPage.Side side){
        this.side = side;
        List<ComponentSingleText> list = new ArrayList<>();
        lines.forEach(s -> Minecraft.getInstance().fontRenderer.listFormattedStringToWidth(s, width).forEach(s1 -> list.add(new ComponentSingleText(s1, side))));
        this.text = list;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, IPage.Side side) {
        if(side == this.side){
            List<ComponentSingleText> text1 = this.text;
            int completeHeight = 0;
            for (int i = 0; i < text1.size(); i++) {
                ComponentSingleText text = text1.get(i);
                text.setLine(i);
                /* todo implement with 1.15 mappings
                GlStateManager.pushMatrix();
                text.draw(gui, left, top + completeHeight, width, height, mouseX, mouseY, side);
                GlStateManager.popMatrix();
                completeHeight += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * text.getScale();*/
            }
        }
    }

}
