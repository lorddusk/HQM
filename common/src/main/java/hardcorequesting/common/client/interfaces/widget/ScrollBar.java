package hardcorequesting.common.client.interfaces.widget;


import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

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
    
    private double scroll;
    private boolean isScrolling;
    private final GuiBase gui;
    
    public ScrollBar(GuiBase gui, int x, int y, int h, int u, int v, int left) {
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.h = h;
        this.u = u;
        this.v = v;
        this.left = left;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isVisible() {
        return true;
    }
    
    @Environment(EnvType.CLIENT)
    protected void onUpdate() {
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices) {
        if (isVisible()) {
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            this.gui.drawRect(matrices, x, y, u, v, SCROLL_WIDTH, h);
            this.gui.drawRect(matrices, x + 1, (int) (y + 1 + scroll), SCROLL_BAR_SRC_X, SCROLL_BAR_SRC_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onClick(int mX, int mY) {
        if (isVisible() && this.gui.inBounds(x, y, SCROLL_WIDTH, h, mX, mY)) {
            isScrolling = true;
            updateScroll(mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onDrag(int mX, int mY) {
        if (isVisible()) {
            updateScroll(mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onRelease(int mX, int mY) {
        if (isVisible()) {
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
    
    public float getScroll() {
        return (float) scroll / (h - SCROLL_BAR_HEIGHT - 2);
    }
    
    public <T> List<T> getVisibleEntries(List<T> list, int visibleEntries) {
        int start = this.isVisible() ? Math.round((list.size() - visibleEntries) * this.getScroll()) : 0;
        int end = Math.min(list.size(), start + visibleEntries);
        return list.subList(start, end);
    }
    
    @Environment(EnvType.CLIENT)
    private void setScroll(double newScroll) {
        double old = scroll;
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
    public void onScroll(double mX, double mY, double scroll) {
        if (isVisible() && this.gui.inBounds(left, y, x + SCROLL_WIDTH - left, h, mX, mY)) {
            setScroll(this.scroll - scroll);
        }
    }
}
