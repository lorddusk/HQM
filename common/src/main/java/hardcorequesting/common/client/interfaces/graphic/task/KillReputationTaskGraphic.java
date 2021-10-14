package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.IntInputMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class KillReputationTaskGraphic extends ReputationTaskGraphic {
    
    private final KillReputationTask task;
    
    public KillReputationTaskGraphic(KillReputationTask task, UUID playerId, GuiQuestBook gui) {
        super(task, playerId, gui, 20);
        this.task = task;
        
        addButton(new LargeButton(gui, "hqm.quest.requirement", 250, 95) {
            @Override
            public boolean isVisible() {
                return Quest.canQuestsBeEdited();
            }
        
            @Override
            public void onClick() {
                IntInputMenu.display(gui, playerId, "hqm.mobTask.reqKills", task.getKillsRequirement(), task::setKills);
            }
        });
    
    }
    
    @Override
    protected boolean shouldShowPlayer() {
        return false;
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
    
        int killCount = task.getKills(playerId);
        if (Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.repKil.kills", killCount, Translator.player(task.getKillsRequirement())), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(matrices, gui.getLinesFromText(killCount == task.getKillsRequirement()
                    ? Translator.translatable("hqm.repKil.killCount", Translator.player(task.getKillsRequirement())).withStyle(ChatFormatting.DARK_GREEN)
                    : Translator.translatable("hqm.repKil.killCountOutOf", killCount, Translator.player(task.getKillsRequirement())), 1F, 130), START_X, START_Y, 1F, 0x404040);
        }
    }
}
