package hqm.client.gui.component;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 */
//TODO
public class ComponentScrollBar implements IRenderer{

    public static int WIDTH_BAR = 7;
    public static int OFFSET = 2;
    public static int HEIGHT = 178;

    private int scrollPosition = 0;
    private List<IRenderer> components = new ArrayList<>();

    public void addComponent(IRenderer component){
        this.components.add(component);
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        this.components.forEach(renderer -> renderer.draw(gui, left + OFFSET, top + OFFSET, width - WIDTH_BAR - OFFSET, height - 2*OFFSET, mouseX, mouseY, side));
        gui.bindTexture(LOC);
        gui.drawTexturedModalRect(left + width - WIDTH_BAR, top + OFFSET, 171, 69, 7, 187);
        gui.drawTexturedModalRect(left + width - WIDTH_BAR + 1 , top + OFFSET + 1 + this.scrollPosition, 250, 167, 5, 6);
    }

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {
        this.components.forEach(renderer -> renderer.mouseClick(gui, left + OFFSET, top + OFFSET, width - WIDTH_BAR - OFFSET, height - 2*OFFSET, mouseX, mouseY, mouseButton, side));
    }

    @Override
    public void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {
        this.components.forEach(renderer -> renderer.mouseRelease(gui, left + OFFSET, top + OFFSET, width - WIDTH_BAR - OFFSET, height - 2*OFFSET, mouseX, mouseY, mouseButton, side));
    }

    @Override
    public void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, IPage.Side side) {
        this.components.forEach(renderer -> renderer.mouseClickMove(gui, left + OFFSET, top + OFFSET, width - WIDTH_BAR - OFFSET, height - 2*OFFSET, mouseX, mouseY, lastMouseX, lastMouseY, mouseButton, ticks, side));
    }

    @Override
    public void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, IPage.Side side) {

    }

}
