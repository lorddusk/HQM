package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class KillReputationTaskGraphic extends ReputationTaskGraphic {
    
    private final KillReputationTask task;
    
    public KillReputationTaskGraphic(KillReputationTask task) {
        super(task, 20);
        this.task = task;
    }
    
    @Override
    protected Player getPlayerForRender(Player player) {
        return null;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        super.draw(matrices, gui, player, mX, mY);
    
        int killCount = task.getData(player).kills;
        if (Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.pluralTranslated(task.getKills() != 1, "hqm.repKil.kills", killCount, task.getKills()), 1F, 130), START_X, START_Y, 1F, 0x404040);
        } else {
            gui.drawString(matrices, gui.getLinesFromText(killCount == task.getKills() ? Translator.pluralTranslated(task.getKills() != 1, "hqm.repKil.killCount", GuiColor.GREEN, task.getKills()) : Translator.translatable("hqm.repKil.killCountOutOf", killCount, task.getKills()), 1F, 130), START_X, START_Y, 1F, 0x404040);
        }
    }
}
