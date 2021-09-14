package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;

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
    private List<FormattedText> lines;
    
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
    public abstract boolean isEnabled(GuiBase gui);
    
    @Environment(EnvType.CLIENT)
    public abstract boolean isVisible(GuiBase gui);
    
    @Environment(EnvType.CLIENT)
    public abstract void onClick(GuiBase gui);
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        if (isVisible(gui)) {
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            boolean enabled = isEnabled(gui);
            gui.drawRect(matrices, x, y, BUTTON_SRC_X + (enabled && inButtonBounds(gui, mX, mY) ? BUTTON_WIDTH : 0), BUTTON_SRC_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
            gui.drawCenteredString(matrices, getName(), x, y, 0.7F, BUTTON_WIDTH, BUTTON_HEIGHT, enabled ? 0x404040 : 0xA0A070);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void renderTooltip(PoseStack matrices, GuiBase gui, int mX, int mY) {
        if (isVisible(gui) && description != null && inButtonBounds(gui, mX, mY)) {
            if (lines == null) {
                lines = gui.getLinesFromText(getDescription(), 1, 200);
            }
            
            gui.renderTooltip(matrices, Language.getInstance().getVisualOrder(lines), mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    protected FormattedText getName() {
        return Translator.translatable(name);
    }
    
    protected FormattedText getDescription() {
        return Translator.translatable(description);
    }
    
}
