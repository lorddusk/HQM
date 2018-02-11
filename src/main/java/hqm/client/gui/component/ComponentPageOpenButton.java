package hqm.client.gui.component;

import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

/**
 * @author canitzp
 */
public class ComponentPageOpenButton implements IRenderer {

    private final int x, y;
    private final IPage page;
    private final IPage.Side side;
    private String text;
    private int width;

    public ComponentPageOpenButton(int x, int y, IPage page, IPage.Side side) {
        this.x = x;
        this.y = y;
        this.page = page;
        this.side = side;
        this.setText(page.getId().toString());
    }

    public ComponentPageOpenButton setText(String text){
        this.text = text;
        this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
        return this;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        if(this.side == side){
            FontRenderer font = gui.mc.fontRenderer;
            font.drawString(this.text, this.x + left - this.width / 2, this.y + top, 0x000000);
        }
    }

    @Override
    public void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {
        if(this.side == side){
            if(mouseX >= this.x + left - this.width / 2 && mouseX <= this.x + left + this.width / 2 && mouseY >= this.y + top && mouseY <= this.y + top + gui.mc.fontRenderer.FONT_HEIGHT){
                gui.setPage(this.page);
            }
        }
    }
}
