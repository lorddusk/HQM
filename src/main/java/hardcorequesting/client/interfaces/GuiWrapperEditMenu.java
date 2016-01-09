package hardcorequesting.client.interfaces;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiWrapperEditMenu extends GuiBase {

    private GuiEditMenu editMenu;


    @Override
    public void setEditMenu(GuiEditMenu editMenu) {

        if (editMenu != null) {
            this.editMenu = editMenu;
        } else {
            this.mc.displayGuiScreen(null);
        }
    }

    public static final ResourceLocation BG_TEXTURE = ResourceHelper.getResource("wrapper");
    public static final ResourceLocation C_BG_TEXTURE = ResourceHelper.getResource("c_wrapper");


    private static final int TEXTURE_WIDTH = 170;
    private static final int TEXTURE_HEIGHT = 234;

    @Override
    public void drawScreen(int mX0, int mY0, float f) {
        boolean doublePage = editMenu.doesRequiredDoublePage();

        this.left = (this.width - (doublePage ? TEXTURE_WIDTH * 2 : TEXTURE_WIDTH)) / 2;
        this.top = (this.height - TEXTURE_HEIGHT) / 2;

        applyColor(0xFFFFFFFF);

        ResourceHelper.bindResource(BG_TEXTURE);


        drawRect(0, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (doublePage) {
            drawRect(TEXTURE_WIDTH, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, RenderRotation.FLIP_HORIZONTAL);
        }

        applyColor(0xFFFFFFFF);

        ResourceHelper.bindResource(MAP_TEXTURE);


        int mX = mX0 - left;
        int mY = mY0 - top;

        if (editMenu != null) {
            editMenu.draw(this, mX, mY);
            editMenu.drawMouseOver(this, mX, mY);
        }
    }

    @Override
    protected void mouseClicked(int mX0, int mY0, int b) {
        int mX = mX0 - left;
        int mY = mY0 - top;

        if (editMenu != null) {
            editMenu.onClick(this, mX, mY, b);
        }
    }

    @Override
    protected void mouseClickMove(int mX0, int mY0, int b, long ticks) {
        int mX = mX0 - left;
        int mY = mY0 - top;

        if (editMenu != null) {
            editMenu.onDrag(this, mX, mY);
        }
    }

    @Override
    protected void mouseReleased(int mX0, int mY0, int b) {
        int mX = mX0 - left;
        int mY = mY0 - top;

        if (editMenu != null) {
            editMenu.onRelease(this, mX, mY);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (editMenu != null) {
            int x = (Mouse.getEventX() * this.width / this.mc.displayWidth) - left;
            int y = (this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1) - top;

            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                editMenu.onScroll(this, x, y, scroll);
            }
        }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        super.keyTyped(c, k);

        if (editMenu != null) {
            editMenu.onKeyTyped(this, c, k);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
