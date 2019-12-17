package hqm.client.gui;

import hqm.HQM;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public interface IRenderer {

    ResourceLocation LOC = new ResourceLocation(HQM.MODID, "textures/gui/questmap.png");

    void draw(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, IPage.Side side);

    default void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, int mouseButton, IPage.Side side){}

    default void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, int mouseButton, IPage.Side side){}

    default void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, double lastMouseX, double lastMouseY, int mouseButton, IPage.Side side){}

    default void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, int scroll, IPage.Side side){}

    void setOffset(int x, int y);

}
