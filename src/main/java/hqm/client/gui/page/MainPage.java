package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public class MainPage implements IPage{

    public static final ResourceLocation LOC_FRONT_TEXT = new ResourceLocation(HQM.MODID, "textures/gui/front.png");

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "main_page");
    }

    @Override
    public void init(GuiQuestBook gui) {
        gui.addRenderer(new IRenderer() {
            @Override
            public void draw(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, Side side) {

            }

            @Override
            public void mouseClick(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int mouseButton, Side side) {
                System.out.println("Click: " + mouseX + " " + mouseY + " " + side);
            }

            @Override
            public void mouseRelease(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int mouseButton, Side side) {
                System.out.println("Release: " + mouseX + " " + mouseY + " " + side);
            }

            @Override
            public void mouseClickMove(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, Side side) {
                System.out.println("Move: " + mouseX + " " + mouseY + " " + lastMouseX + " " + lastMouseY + " " + side);
            }

            @Override
            public void mouseScroll(GuiQuestBook gui, int left, int top, int mouseX, int mouseY, int scroll, Side side) {
                System.out.println("Scroll: " + mouseX + " " + mouseY + " " + scroll + " " + side);
            }
        });
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {
        switch (side){
            case RIGHT: {

                break;
            }
            case LEFT: {
                gui.bindTexture(LOC_FRONT_TEXT);
                Gui.drawScaledCustomSizeModalRect(pageLeft, pageTop, 0, 0, 153, 253, 140, 190, 280, 360);
                break;
            }
        }
    }
}
