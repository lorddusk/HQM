package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.Colors;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.component.ComponentScrollPane;
import hqm.client.gui.component.ComponentSingleText;
import hqm.quest.Quest;
import hqm.quest.QuestLine;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * @author canitzp
 */
public class QuestPage implements IPage {

    private Quest quest;
    private QuestLine questLine;

    public QuestPage(QuestLine questLine, Quest quest){
        this.questLine = questLine;
        this.quest = quest;
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "quest_page");
    }

    @Override
    public void init(GuiQuestBook gui) {
        gui.addRenderer(new ComponentSingleText(this.quest.name, Side.LEFT));
        ComponentScrollPane<ComponentSingleText> descPane = new ComponentScrollPane<>(Side.LEFT);
        if(this.quest.desc != null && !this.quest.desc.isEmpty()){
            List<ComponentSingleText> list = ComponentSingleText.from(this.quest.desc, Math.round((GuiQuestBook.PAGE_WIDTH - ComponentScrollPane.WIDTH_BAR)/ 0.8F), Side.LEFT);
            list.forEach(componentSingleText -> componentSingleText.setScale(0.8F));
            list.forEach(descPane::addComponent);
        } else {
            descPane.addComponent(new ComponentSingleText("No Description provided!", Side.LEFT).setColor(Colors.LIGHT_GRAY).setScale(0.8F));
        }
        descPane.setHeight(GuiQuestBook.PAGE_HEIGHT / 2 - 25);
        descPane.setOffset(0, Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 2);
        gui.addRenderer(descPane);
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {

    }
}
