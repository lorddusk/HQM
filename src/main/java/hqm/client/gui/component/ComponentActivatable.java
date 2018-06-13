package hqm.client.gui.component;

import hqm.client.gui.AbstractRender;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;

import java.util.function.Supplier;

/**
 * @author canitzp
 */
public class ComponentActivatable extends AbstractRender {

    private final Supplier<Boolean> supplier;
    private final IRenderer component;

    public ComponentActivatable(Supplier<Boolean> supplier, IRenderer component) {
        this.supplier = supplier;
        this.component = component;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        if(this.supplier.get()){
            this.component.draw(gui, left, top, width, height, mouseX, mouseY, side);
        }
    }

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {
        if(this.supplier.get()){
            this.component.mouseClick(gui, left, top, width, height, mouseX, mouseY, mouseButton, side);
        }
    }

    @Override
    public void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {
        if(this.supplier.get()){
            this.component.mouseRelease(gui, left, top, width, height, mouseX, mouseY, mouseButton, side);
        }
    }

    @Override
    public void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, IPage.Side side) {
        if(this.supplier.get()){
            this.component.mouseClickMove(gui, left, top, width, height, mouseX, mouseY, lastMouseX, lastMouseY, mouseButton, ticks, side);
        }
    }

    @Override
    public void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, IPage.Side side) {
        if(this.supplier.get()){
            this.component.mouseScroll(gui, left, top, width, height, mouseX, mouseY, scroll, side);
        }
    }
}
