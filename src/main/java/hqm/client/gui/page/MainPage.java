package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.Colors;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.component.ComponentPageOpenButton;
import hqm.client.gui.component.ComponentSingleText;
import hqm.client.gui.component.ComponentTextArea;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * @author canitzp
 */
public class MainPage implements IPage{

    public static final ResourceLocation LOC_FRONT_TEXT = new ResourceLocation(HQM.MODID, "textures/gui/front.png");

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "main_page");
    }

    @Override
    public void init(GuiQuestBook gui) {
        List<ComponentSingleText> text = ComponentSingleText.from(gui.getQuestbook().getDescription(), GuiQuestBook.PAGE_WIDTH, Side.RIGHT);
        text.forEach(componentSingleText -> componentSingleText.setScale(0.85F));
        gui.addRenderer(new ComponentTextArea(text, Side.RIGHT));
        gui.addRenderer(new ComponentPageOpenButton(GuiQuestBook.PAGE_WIDTH / 2, GuiQuestBook.PAGE_HEIGHT + 2, new InformationPage(), Side.LEFT).setText(new ComponentSingleText("Click here to start", Side.LEFT).setScale(0.75F).setColor(Colors.LIGHT_GRAY)));
        gui.setRewindPage(this);
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {
        switch (side){
            case LEFT: {
                gui.bindTexture(gui.getQuestbook().getImage());
                Gui.drawScaledCustomSizeModalRect(pageLeft, pageTop - 10, 0, 0, 153, 253, 140, 190, 280, 360);
                break;
            }
        }
    }
}
