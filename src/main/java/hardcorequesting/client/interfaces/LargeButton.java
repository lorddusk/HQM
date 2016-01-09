package hardcorequesting.client.interfaces;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.Translator;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.util.List;

public abstract class LargeButton {
    private static final int BUTTON_SRC_X = 54;
    private static final int BUTTON_SRC_Y = 235;
    private static final int BUTTON_WIDTH = 57;
    private static final int BUTTON_HEIGHT = 18;

    private String name;
    private String description;
    private int x;
    private int y;

    public LargeButton(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public LargeButton(String name, String description, int x, int y) {
        this(name, x, y);
        this.description = description;
    }

    @SideOnly(Side.CLIENT)
    public boolean inButtonBounds(GuiBase gui, int mX, int mY) {
        return gui.inBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, mX, mY);
    }

    @SideOnly(Side.CLIENT)
    public abstract boolean isEnabled(GuiBase gui, EntityPlayer player);

    @SideOnly(Side.CLIENT)
    public abstract boolean isVisible(GuiBase gui, EntityPlayer player);

    @SideOnly(Side.CLIENT)
    public abstract void onClick(GuiBase gui, EntityPlayer player);


    @SideOnly(Side.CLIENT)
    public void draw(GuiBase gui, EntityPlayer player, int mX, int mY) {
        if (isVisible(gui, player)) {

            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            GL11.glColor3f(1F, 1F, 1F);
            boolean enabled = isEnabled(gui, player);
            gui.drawRect(x, y, BUTTON_SRC_X + (enabled && inButtonBounds(gui, mX, mY) ? BUTTON_WIDTH : 0), BUTTON_SRC_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
            gui.drawCenteredString(getName(), x, y, 0.7F, BUTTON_WIDTH, BUTTON_HEIGHT, enabled ? 0x404040 : 0xA0A070);
        }
    }


    private List<String> lines;

    @SideOnly(Side.CLIENT)
    public void drawMouseOver(GuiBase gui, EntityPlayer player, int mX, int mY) {
        if (isVisible(gui, player) && description != null && inButtonBounds(gui, mX, mY)) {
            if (lines == null) {
                lines = gui.getLinesFromText(getDescription(), 1, 200);
            }

            gui.drawMouseOver(lines, mX + gui.left, mY + gui.top);
        }
    }

    protected String getName() {
        return Translator.translate(name);
    }

    protected String getDescription() {
        return Translator.translate(description);
    }

}
