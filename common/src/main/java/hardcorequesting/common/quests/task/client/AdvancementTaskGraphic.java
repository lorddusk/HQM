package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickAdvancementMenu;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class AdvancementTaskGraphic extends IconTaskGraphic<GetAdvancementTask.Part> {
    
    private final GetAdvancementTask task;
    
    public AdvancementTaskGraphic(GetAdvancementTask task, PartList<GetAdvancementTask.Part> parts, UUID playerId) {
        super(task, parts, playerId);
        this.task = task;
        addDetectButton(task);
    }
    
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, GetAdvancementTask.Part part, int id, int x, int y) {
        if (task.advanced(id, playerId)) {
            gui.drawString(matrices, Translator.translatable("hqm.advancementMenu.visited", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        }
    }
    
    @Override
    protected boolean handlePartClick(GuiQuestBook gui, EditMode mode, GetAdvancementTask.Part part, int id) {
        if (mode == EditMode.LOCATION) {
            PickAdvancementMenu.display(gui, playerId, part.getAdvancement(),
                    result -> task.setAdvancement(id, result));
            return true;
        } else {
            return super.handlePartClick(gui, mode, part, id);
        }
    }
}
