package hardcorequesting.client.interfaces;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ScrollBar {

    private static final int SCROLL_WIDTH = 7;
    private static final int SCROLL_BAR_WIDTH = 5;
    private static final int SCROLL_BAR_HEIGHT = 6;
    private static final int SCROLL_BAR_SRC_X = 250;
    private static final int SCROLL_BAR_SRC_Y = 167;

    private int x;
    private int y;
    private int h;
    private int u;
    private int v;
    private int left;

    private int scroll;
    private boolean isScrolling;

    public ScrollBar(int x, int y, int h, int u, int v, int left) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.u = u;
        this.v = v;
        this.left = left;
    }

    @SideOnly(Side.CLIENT)
    public boolean isVisible(GuiBase gui) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    protected void onUpdate() {
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiBase gui) {
        if (isVisible(gui)) {
            gui.drawRect(x, y, u, v, SCROLL_WIDTH, h);
            gui.drawRect(x + 1, y + 1 + scroll, SCROLL_BAR_SRC_X, SCROLL_BAR_SRC_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT);
        }
    }

    @SideOnly(Side.CLIENT)
    public void onClick(GuiBase gui, int mX, int mY) {
        if (isVisible(gui) && gui.inBounds(x, y, SCROLL_WIDTH, h, mX, mY)) {
            isScrolling = true;
            updateScroll(mY);
        }
    }

    @SideOnly(Side.CLIENT)
    public void onDrag(GuiBase gui, int mX, int mY) {
        if (isVisible(gui)) {
            updateScroll(mY);
        }
    }

    @SideOnly(Side.CLIENT)
    public void onRelease(GuiBase gui, int mX, int mY) {
        if (isVisible(gui)) {
            updateScroll(mY);
            isScrolling = false;
        }
    }

    private void updateScroll(int mY) {
        if (isScrolling) {
            setScroll(mY - y - SCROLL_BAR_HEIGHT / 2);
        }
    }

    public int getRawScroll() {
        return scroll;
    }

    public float getScroll() {
        return (float) scroll / (h - SCROLL_BAR_HEIGHT - 2);
    }

    public void resetScroll() {
        scroll = 0;
    }

    private void setScroll(int newScroll) {
        int old = scroll;
        scroll = newScroll;
        if (scroll < 0) {
            scroll = 0;
        } else if (scroll > h - SCROLL_BAR_HEIGHT - 2) {
            scroll = h - SCROLL_BAR_HEIGHT - 2;
        }
        if (scroll != old) {
            onUpdate();
        }
    }

    public void onScroll(GuiBase gui, int mX, int mY, int scroll) {
        if (isVisible(gui) && gui.inBounds(left, y, x + SCROLL_WIDTH - left, h, mX, mY)) {
            setScroll(this.scroll - scroll / 20);
        }
    }
}
