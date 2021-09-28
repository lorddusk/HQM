package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
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

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class DeathTaskGraphic extends TaskGraphic {
    
    private final DeathTask task;
    
    public DeathTaskGraphic(DeathTask task, UUID playerId, GuiQuestBook gui) {
        super(playerId, gui, task);
        this.task = task;
    
        addButton(new LargeButton(gui, "hqm.quest.requirement", 185, 200) {
            @Override
            public boolean isVisible() {
                return Quest.canQuestsBeEdited();
            }
        
            @Override
            public void onClick() {
                IntInputMenu.display(gui, playerId, "hqm.deathTask.reqDeathCount", task.getDeathsRequired(), task::setDeaths);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        int died = task.getDeaths(playerId);
        FormattedText text = died == task.getDeathsRequired()
                ? Translator.pluralTranslated(task.getDeathsRequired() != 0, "hqm.deathMenu.deaths", GuiColor.GREEN, task.getDeathsRequired())
                : Translator.pluralTranslated(task.getDeathsRequired() != 0, "hqm.deathMenu.deathsOutOf", died, task.getDeathsRequired());
    
        gui.drawString(matrices, gui.getLinesFromText(text, 1F, 130), START_X, START_Y, 1F, 0x404040);
    
        super.draw(matrices, mX, mY);
    }
}
