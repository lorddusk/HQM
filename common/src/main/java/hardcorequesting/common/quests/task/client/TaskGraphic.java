package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public interface TaskGraphic {
    int START_X = 180;
    int START_Y = 95;
    
    void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY);
    
    void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b);
    
}
