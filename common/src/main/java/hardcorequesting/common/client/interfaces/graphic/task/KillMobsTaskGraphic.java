package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickMobMenu;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class KillMobsTaskGraphic extends IconTaskGraphic<KillMobsTask.Part> {
    
    private final KillMobsTask task;
    
    public KillMobsTaskGraphic(KillMobsTask task, UUID playerId, GuiQuestBook gui) {
        super(task, playerId, gui);
        this.task = task;
    }
    
    @Override
    protected void drawElementText(PoseStack matrices, KillMobsTask.Part part, int index, int x, int y) {
        int killed = task.killed(index, playerId);
        if (killed == part.getCount()) {
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.allKilled").withStyle(ChatFormatting.DARK_GREEN), x, y, 0.7F, 0x404040);
        } else {
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.partKills", killed, (100 * killed / part.getCount())), x, y, 0.7F, 0x404040);
        }
        gui.drawString(matrices, Translator.translatable("hqm.mobTask.totalKills", part.getCount()), x, y + 6, 0.7F, 0x404040);
    }
    
    @Override
    protected boolean handleEditPartClick(EditMode mode, KillMobsTask.Part part, int id) {
        if (mode == EditMode.MOB) {
            PickMobMenu.display(gui, part.getMob(), part.getCount(), "mobTask",
                    result -> task.setInfo(id, result.getMobId(), result.getAmount()));
            return true;
        } else {
            return super.handleEditPartClick(mode, part, id);
        }
    }
}
