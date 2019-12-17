package hqm.client.gui.component;

import hqm.client.gui.AbstractRender;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;

/**
 * @author canitzp
 */
public class ComponentPageOpenButton extends AbstractRender {

    private final int x, y;
    private final IPage page;
    private final IPage.Side side;
    private ComponentSingleText text;
    private int width;

    public ComponentPageOpenButton(int x, int y, IPage page, IPage.Side side) {
        this.x = x;
        this.y = y;
        this.page = page;
        this.side = side;
        this.setText(new ComponentSingleText("No button text", side));
    }

    public ComponentPageOpenButton setText(ComponentSingleText text){
        this.text = text;
        this.width = text.getWidth();
        return this;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, IPage.Side side) {
        if(this.side == side){
            this.text.draw(gui, this.x + left - this.width / 2, this.y + top, width, height, mouseX, mouseY, side);
        }
    }

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, int mouseButton, IPage.Side side) {
        if(this.side == side){
            if(mouseX >= this.x + left - this.width / 2 && mouseX <= this.x + left + this.width / 2 && mouseY >= this.y + top && mouseY <= this.y + top + this.text.getScale() * gui.getMinecraft().fontRenderer.FONT_HEIGHT){
                gui.setPage(this.page, true);
            }
        }
    }
}
