package hqm.client.gui;

/**
 * @author canitzp
 */
public abstract class AbstractRender implements IRenderer {

    private int xOffset = 0, yOffset = 0;

    @Override
    public void setOffset(int x, int y) {
        this.xOffset = x;
        this.yOffset = y;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }
}
