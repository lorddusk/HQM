package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

/**
 * Abstract class that represents a screen section.
 */
@Environment(EnvType.CLIENT)
public abstract class Graphic {
    
    public final void drawFull(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        draw(matrices, gui, player, mX, mY);
        drawTooltip(matrices, gui, player, mX, mY);
    }
    
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
    }
    
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
    }
    
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
    
    }
}