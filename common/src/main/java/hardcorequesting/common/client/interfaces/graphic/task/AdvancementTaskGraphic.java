package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickAdvancementMenu;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class AdvancementTaskGraphic extends IconTaskGraphic<GetAdvancementTask.Part> {
    
    private final GetAdvancementTask task;
    
    public AdvancementTaskGraphic(GetAdvancementTask task, UUID playerId, GuiQuestBook gui) {
        super(task, playerId, gui);
        this.task = task;
        addDetectButton(task);
    }
    
    @Override
    protected void drawElementText(PoseStack matrices, GetAdvancementTask.Part part, int id, int x, int y) {
        if (task.advanced(id, playerId)) {
            gui.drawString(matrices, Translator.translatable("hqm.advancementMenu.visited").withStyle(ChatFormatting.DARK_GREEN), x, y, 0.7F, 0x404040);
        }
    }
    
    @Override
    protected boolean handlePartClick(EditMode mode, GetAdvancementTask.Part part, int id) {
        if (mode == EditMode.LOCATION) {
            PickAdvancementMenu.display(gui, part.getAdvancement(),
                    result -> task.setAdvancement(id, result));
            return true;
        } else {
            return super.handlePartClick(mode, part, id);
        }
    }
}
