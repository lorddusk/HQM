package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.IntInputMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class KillReputationTaskGraphic extends ReputationTaskGraphic {
    
    private final KillReputationTask task;
    
    public KillReputationTaskGraphic(KillReputationTask task, PartList<ReputationTask.Part> parts, UUID playerId) {
        super(task, parts, playerId, 20);
        this.task = task;
        
        addButton(new LargeButton("hqm.quest.requirement", 250, 95) {
            @Override
            public boolean isEnabled() {
                return true;
            }
        
            @Override
            public boolean isVisible() {
                return Quest.canQuestsBeEdited();
            }
        
            @Override
            public void onClick(GuiBase gui) {
                IntInputMenu.display(gui, playerId, "hqm.mobTask.reqKills", task.getKillsRequirement(), task::setKills);
            }
        });
    
    }
    
    @Override
    protected boolean shouldShowPlayer() {
        return false;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
    
        int killCount = task.getKills(playerId);
        if (Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.pluralTranslated(task.getKillsRequirement() != 1, "hqm.repKil.kills", killCount, task.getKillsRequirement()), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(matrices, gui.getLinesFromText(killCount == task.getKillsRequirement() ? Translator.pluralTranslated(task.getKillsRequirement() != 1, "hqm.repKil.killCount", GuiColor.GREEN, task.getKillsRequirement()) : Translator.translatable("hqm.repKil.killCountOutOf", killCount, task.getKillsRequirement()), 1F, 130), START_X, START_Y, 1F, 0x404040);
        }
    }
}
