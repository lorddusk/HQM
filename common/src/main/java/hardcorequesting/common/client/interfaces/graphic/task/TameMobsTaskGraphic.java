package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickMobMenu;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class TameMobsTaskGraphic extends IconTaskGraphic<TameMobsTask.Part> {
    
    private final TameMobsTask task;
    
    public TameMobsTaskGraphic(TameMobsTask task, UUID playerId, GuiQuestBook gui) {
        super(task, playerId, gui);
        this.task = task;
    }
    
    @Override
    protected void drawElementText(PoseStack matrices, TameMobsTask.Part part, int index, int x, int y) {
        int tamed = task.tamed(index, playerId);
        if (tamed == part.getCount()) {
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.allTamed").withStyle(ChatFormatting.DARK_GREEN), x, y, 0.7F, 0x404040);
        } else {
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.partTames", tamed, (100 * tamed / part.getCount())), x, y, 0.7F, 0x404040);
        }
        gui.drawString(matrices, Translator.translatable("hqm.tameTask.totalTames", part.getCount()), x, y + 6, 0.7F, 0x404040);
    }
    
    @Override
    protected boolean handlePartClick(EditMode mode, TameMobsTask.Part part, int id) {
        if (mode == EditMode.MOB) {
            PickMobMenu.display(gui, playerId, part.getTame() == null ? null : ResourceLocation.tryParse(part.getTame()), part.getCount(), "tameTask",
                    PickMobMenu.EXTRA_TAME_ENTRIES, result -> task.setInfo(id, result.getMobId().toString(), result.getAmount()));
            return true;
        } else {
            return super.handlePartClick(mode, part, id);
        }
    }
}
