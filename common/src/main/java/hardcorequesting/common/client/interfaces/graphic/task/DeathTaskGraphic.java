package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.IntInputMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.DeathTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class DeathTaskGraphic extends TaskGraphic {
    
    private final DeathTask task;
    
    public DeathTaskGraphic(DeathTask task, UUID playerId, GuiQuestBook gui) {
        super(playerId, gui, task);
        this.task = task;
    
        addClickable(new LargeButton(gui, "hqm.quest.requirement", 185, 200) {
            @Override
            public boolean isVisible() {
                return Quest.canQuestsBeEdited();
            }
        
            @Override
            public void onClick() {
                IntInputMenu.display(gui, "hqm.deathTask.reqDeathCount", task.getDeathsRequired(), task::setDeaths);
            }
        });
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        int died = task.getDeaths(playerId);
        FormattedText text = died == task.getDeathsRequired()
                ? Translator.translatable("hqm.deathMenu.deaths", Translator.plural("hqm.times", task.getDeathsRequired())).withStyle(ChatFormatting.DARK_GREEN)
                : Translator.translatable("hqm.deathMenu.deathsOutOf", died, Translator.plural("hqm.times", task.getDeathsRequired()));
    
        gui.drawString(graphics, gui.getLinesFromText(text, 1F, 130), START_X, START_Y, 1F, 0x404040);
    
        super.draw(graphics, mX, mY);
    }
}
