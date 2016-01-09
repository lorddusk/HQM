package hardcorequesting.client.interfaces;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public enum GuiColor {
    BLACK(0, 0.1F, 0.1F, 0.1F),
    BLUE(1, 0.2F, 0.3F, 0.7F),
    GREEN(2, 0.4F, 0.5F, 0.2F),
    CYAN(3, 0.3F, 0.5F, 0.6F),
    RED(4, 0.6F, 0.2F, 0.2F),
    PURPLE(5, 0.5F, 0.25F, 0.7F),
    ORANGE(6, 0.85F, 0.5F, 0.2F),
    LIGHT_GRAY(7, 0.6F, 0.6F, 0.6F),
    GRAY(8, 0.3F, 0.3F, 0.3F),
    LIGHT_BLUE(9, 0.4F, 0.6F, 0.85F),
    LIME(10, 0.5F, 0.8F, 0.1F),
    TURQUOISE(11, 0F, 1F, 0.9F),
    PINK(12, 0.95F, 0.5F, 0.65F),
    MAGENTA(13, 0.7F, 0.3F, 0.85F),
    YELLOW(14, 0.9F, 0.9F, 0.2F),
    WHITE(15, 0.4F, 0.3F, 0.2F);


    private String tagColor;
    private float red;
    private float green;
    private float blue;
    private int hex;

    GuiColor(int number, float red, float green, float blue) {
        this.tagColor = "\u00a7" + Integer.toHexString(number);
        this.red = red;
        this.green = green;
        this.blue = blue;

        hex = (0xFF << 24) | ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | ((int) (blue * 255) << 0);
    }

    @SideOnly(Side.CLIENT)
    public void applyColor() {
        GL11.glColor4f(red, green, blue, 1F);
    }

    public int getHexColor() {
        return hex;
    }

    @Override
    public String toString() {
        return tagColor;
    }

    public String getName() {
        return (super.toString().charAt(0) + super.toString().substring(1).toLowerCase()).replace("_", " ");
    }
}

