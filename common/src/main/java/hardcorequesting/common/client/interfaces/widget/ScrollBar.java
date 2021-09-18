package hardcorequesting.common.client.interfaces.widget;


import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ScrollBar {
    
    private static final int SCROLL_WIDTH = 7;
    private static final int SCROLL_BAR_WIDTH = 5;
    private static final int SCROLL_BAR_HEIGHT = 6;
    private static final int SCROLL_BAR_SRC_X = 250;
    private static final int SCROLL_BAR_SRC_Y = 167;
    
    private final int x;
    private final int y;
    private final int h;
    private final int u;
    private final int v;
    private final int left;
    
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
    
    @Environment(EnvType.CLIENT)
    public boolean isVisible(GuiBase gui) {
        return true;
    }
    
    @Environment(EnvType.CLIENT)
    protected void onUpdate() {
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiBase gui) {
        if (isVisible(gui)) {
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            gui.drawRect(matrices, x, y, u, v, SCROLL_WIDTH, h);
            gui.drawRect(matrices, x + 1, y + 1 + scroll, SCROLL_BAR_SRC_X, SCROLL_BAR_SRC_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onClick(GuiBase gui, int mX, int mY) {
        if (isVisible(gui) && gui.inBounds(x, y, SCROLL_WIDTH, h, mX, mY)) {
            isScrolling = true;
            updateScroll(mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onDrag(GuiBase gui, int mX, int mY) {
        if (isVisible(gui)) {
            updateScroll(mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onRelease(GuiBase gui, int mX, int mY) {
        if (isVisible(gui)) {
            updateScroll(mY);
            isScrolling = false;
        }
    }
    
    @Environment(EnvType.CLIENT)
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
    
    @Environment(EnvType.CLIENT)
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
    
    public void resetScroll() {
        scroll = 0;
    }
    
    @Environment(EnvType.CLIENT)
    public void onScroll(GuiBase gui, double mX, double mY, double scroll) {
        if (isVisible(gui) && gui.inBounds(left, y, x + SCROLL_WIDTH - left, h, mX, mY)) {
            setScroll((int) (this.scroll - scroll / 20));
        }
    }
}
