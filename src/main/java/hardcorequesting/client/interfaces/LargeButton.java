package hardcorequesting.client.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

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
    private List<StringVisitable> lines;
    
    public LargeButton(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
    
    public LargeButton(String name, String description, int x, int y) {
        this(name, x, y);
        this.description = description;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean inButtonBounds(GuiBase gui, int mX, int mY) {
        return gui.inBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, mX, mY);
    }
    
    @Environment(EnvType.CLIENT)
    public abstract boolean isEnabled(GuiBase gui, PlayerEntity player);
    
    @Environment(EnvType.CLIENT)
    public abstract boolean isVisible(GuiBase gui, PlayerEntity player);
    
    @Environment(EnvType.CLIENT)
    public abstract void onClick(GuiBase gui, PlayerEntity player);
    
    @Environment(EnvType.CLIENT)
    public void draw(MatrixStack matrices, GuiBase gui, PlayerEntity player, int mX, int mY) {
        if (isVisible(gui, player)) {
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.color3f(1F, 1F, 1F);
            boolean enabled = isEnabled(gui, player);
            gui.drawRect(x, y, BUTTON_SRC_X + (enabled && inButtonBounds(gui, mX, mY) ? BUTTON_WIDTH : 0), BUTTON_SRC_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
            gui.drawCenteredString(matrices, getName(), x, y, 0.7F, BUTTON_WIDTH, BUTTON_HEIGHT, enabled ? 0x404040 : 0xA0A070);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void renderTooltip(MatrixStack matrices, GuiBase gui, PlayerEntity player, int mX, int mY) {
        if (isVisible(gui, player) && description != null && inButtonBounds(gui, mX, mY)) {
            if (lines == null) {
                lines = gui.getLinesFromText(getDescription(), 1, 200);
            }
            
            gui.renderOrderedTooltip(matrices, Language.getInstance().reorder(lines), mX + gui.left, mY + gui.top);
        }
    }
    
    protected StringVisitable getName() {
        return Translator.translated(name);
    }
    
    protected StringVisitable getDescription() {
        return Translator.translated(description);
    }
    
}
