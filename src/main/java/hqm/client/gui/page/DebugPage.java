package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import hqm.client.gui.component.ComponentTextField;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public class DebugPage implements IPage {

    private String lastClick = "No click", lastRelease = "No release", lastClickMove = "No move", lastScroll = "No scroll";

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "debug_page");
    }

    @Override
    public void init(GuiQuestBook gui) {
        gui.addRenderer(new ComponentTextField(false));
        gui.addRenderer(new IRenderer() {
            @Override
            public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, Side side) {}

            @Override
            public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, Side side) {
                lastClick = String.format("Mouse Click: x=%d, y=%d, button=%d, side=%s", mouseX, mouseY, mouseButton, side);
            }

            @Override
            public void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, Side side) {
                lastRelease = String.format("Mouse Release: x=%d, y=%d, button=%d, side=%s", mouseX, mouseY, mouseButton, side);
            }

            @Override
            public void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, Side side) {
                lastClickMove = String.format("Mouse Move: x=%d, y=%d, lastX=%d, lastY=%d, button=%d, lenght=%d side=%s", mouseX, mouseY, lastMouseX, lastMouseY, mouseButton, ticks, side);
            }

            @Override
            public void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, Side side) {
                lastScroll = String.format("Mouse Scroll: x=%d, y=%d, scroll=%d, side=%s", mouseX, mouseY, scroll, side);
            }

            @Override
            public void setOffset(int x, int y) {

            }
        });
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {
        Gui.drawRect(pageLeft, pageTop, pageLeft + GuiQuestBook.PAGE_WIDTH, pageTop + GuiQuestBook.PAGE_HEIGHT, 0xFF008000);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.9F, 0.9F, 1);
        FontRenderer font = gui.mc.fontRenderer;
        font.drawString(lastClick, 5, 5, 0xFFFFFF, true);
        font.drawString(lastRelease, 5, 15, 0xFFFFFF, true);
        font.drawString(lastClickMove, 5, 25, 0xFFFFFF, true);
        font.drawString(lastScroll, 5, 35, 0xFFFFFF, true);
        GlStateManager.popMatrix();
    }

}