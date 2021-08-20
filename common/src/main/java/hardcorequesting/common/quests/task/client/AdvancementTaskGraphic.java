package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickAdvancementMenu;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class AdvancementTaskGraphic extends IconTaskGraphic<GetAdvancementTask.Part> {
    
    private final GetAdvancementTask task;
    
    public AdvancementTaskGraphic(GetAdvancementTask task) {
        super(task);
        this.task = task;
    }
    
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, GetAdvancementTask.Part part, int id, int x, int y) {
        if (task.advanced(id, player)) {
            gui.drawString(matrices, Translator.translatable("hqm.advancementMenu.visited", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        }
    }
    
    @Override
    protected void handlePartClick(GuiQuestBook gui, Player player, EditMode mode, GetAdvancementTask.Part part, int id) {
        if (mode == EditMode.LOCATION) {
            PickAdvancementMenu.display(gui, player, part.getAdvancement(),
                    result -> task.setAdvancement(id, result));
        } else {
            super.handlePartClick(gui, player, mode, part, id);
        }
    }
}
