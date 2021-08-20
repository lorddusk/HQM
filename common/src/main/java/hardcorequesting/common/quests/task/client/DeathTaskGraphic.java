package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.task.DeathTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class DeathTaskGraphic implements TaskGraphic {
    
    private final DeathTask task;
    
    public DeathTaskGraphic(DeathTask task) {
        this.task = task;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        int died = task.getData(player).getDeaths();
        FormattedText text = died == task.getDeaths()
                ? Translator.pluralTranslated(task.getDeaths() != 0, "hqm.deathMenu.deaths", GuiColor.GREEN, task.getDeaths())
                : Translator.pluralTranslated(task.getDeaths() != 0, "hqm.deathMenu.deathsOutOf", died, task.getDeaths());
    
        gui.drawString(matrices, gui.getLinesFromText(text, 1F, 130), START_X, START_Y, 1F, 0x404040);
    }
    
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
    
    }
}
