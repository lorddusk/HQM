package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public class TeamPage implements IPage {

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "team_page");
    }

    @Override
    public void init(GuiQuestBook gui) {

    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, double mouseX, double mouseY, Side side) {

    }
}
