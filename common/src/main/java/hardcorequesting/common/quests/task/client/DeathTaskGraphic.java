package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.IntInputMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.DeathTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class DeathTaskGraphic extends TaskGraphic {
    
    private final DeathTask task;
    
    public DeathTaskGraphic(DeathTask task) {
        this.task = task;
    
        addButton(new LargeButton("hqm.quest.requirement", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
        
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return Quest.canQuestsBeEdited();
            }
        
            @Override
            public void onClick(GuiBase gui, Player player) {
                IntInputMenu.display(gui, player, "hqm.deathTask.reqDeathCount", task.getDeathsRequired(), task::setDeaths);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        int died = task.getDeaths(player.getUUID());
        FormattedText text = died == task.getDeathsRequired()
                ? Translator.pluralTranslated(task.getDeathsRequired() != 0, "hqm.deathMenu.deaths", GuiColor.GREEN, task.getDeathsRequired())
                : Translator.pluralTranslated(task.getDeathsRequired() != 0, "hqm.deathMenu.deathsOutOf", died, task.getDeathsRequired());
    
        gui.drawString(matrices, gui.getLinesFromText(text, 1F, 130), START_X, START_Y, 1F, 0x404040);
    
        super.draw(matrices, gui, player, mX, mY);
    }
}
