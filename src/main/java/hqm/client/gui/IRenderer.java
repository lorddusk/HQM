package hqm.client.gui;

import hqm.HQM;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public interface IRenderer {

    ResourceLocation LOC = new ResourceLocation(HQM.MODID, "textures/gui/questmap.png");

    void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side);

    default void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side){}

    default void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side){}

    default void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, IPage.Side side){}

    default void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, IPage.Side side){}

    default int getHeight(GuiQuestBook gui, int width, int height, IPage.Side side){
        return height;
    }

}
