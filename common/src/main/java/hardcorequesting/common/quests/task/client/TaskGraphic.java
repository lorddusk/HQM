package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.task.QuestTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

public interface TaskGraphic {
    int START_X = QuestTask.START_X;
    int START_Y = QuestTask.START_Y;
    
    @Environment(EnvType.CLIENT)
    void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY);
    
    @Environment(EnvType.CLIENT)
    void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b);
    
}
