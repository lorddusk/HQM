package hqm.client.gui;

/**
 * @author canitzp
 */
public interface IRenderer {

    void draw(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, IPage.Side side);

    void mouseClick(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int mouseButton, IPage.Side side);

    void mouseRelease(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int mouseButton, IPage.Side side);

    void mouseClickMove(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, IPage.Side side);

    void mouseScroll(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int scroll, IPage.Side side);

}
