package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.Colors;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.quest.QuestLine;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public class QuestTreePage implements IPage {

    private QuestLine questLine;

    public QuestTreePage(QuestLine questLine) {
        this.questLine = questLine;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "quests_page");
    }

    @Override
    public void init(GuiQuestBook gui) {

    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {
        gui.mc.fontRenderer.drawString(String.valueOf(questLine.getQuests().size()), pageLeft, pageTop, Colors.BLACK);
    }
}
