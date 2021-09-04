package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.Graphic;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public abstract class TaskGraphic extends Graphic {
    protected static final int START_X = 180;
    protected static final int START_Y = 95;
    
    public abstract void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY);
    
    public abstract void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b);
    
}
