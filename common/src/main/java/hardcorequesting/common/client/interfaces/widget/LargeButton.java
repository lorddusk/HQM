package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.Nullable;

public abstract class LargeButton implements Drawable, Clickable {
    
    private static final int BUTTON_SRC_X = 54;
    private static final int BUTTON_SRC_Y = 235;
    private static final int BUTTON_WIDTH = 57;
    private static final int BUTTON_HEIGHT = 18;
    
    private String name;
    private String description;
    private int x;
    private int y;
    private final GuiBase gui;
    
    public LargeButton(GuiBase gui, String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.gui = gui;
    }
    
    public LargeButton(GuiBase gui, String name, String description, int x, int y) {
        this(gui, name, x, y);
        this.description = description;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean inButtonBounds(int mX, int mY) {
        return this.gui.inBounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, mX, mY);
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isEnabled() {
        return true;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isVisible() {
        return true;
    }
    
    @Environment(EnvType.CLIENT)
    public abstract void onClick();
    
    @Override
    public boolean onClick(int mX, int mY) {
        if (isVisible() && isEnabled() && inButtonBounds(mX, mY)) {
            onClick();
            return true;
        }
        return false;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void render(GuiGraphics graphics, int mX, int mY) {
        if (isVisible()) {
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            boolean enabled = isEnabled();
            this.gui.drawRect(graphics, GuiBase.MAP_TEXTURE, x, y, BUTTON_SRC_X + (enabled && inButtonBounds(mX, mY) ? BUTTON_WIDTH : 0), BUTTON_SRC_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
            this.gui.drawCenteredString(graphics, getName(), x, y, 0.7F, BUTTON_WIDTH, BUTTON_HEIGHT, enabled ? 0x404040 : 0xA0A070);
        }
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void renderTooltip(GuiGraphics graphics, int mX, int mY) {
        if (isVisible() && inButtonBounds(mX, mY)) {
            FormattedText description = getDescription();
            if (description != null) {
                var lines = this.gui.getLinesFromText(getDescription(), 1, 200);
    
                this.gui.renderTooltipL(graphics, lines, mX + this.gui.getLeft(), mY + this.gui.getTop());
            }
        }
    }
    
    protected FormattedText getName() {
        return Translator.translatable(name);
    }
    
    @Nullable
    protected FormattedText getDescription() {
        return description != null ? Translator.translatable(description) : null;
    }
    
}
