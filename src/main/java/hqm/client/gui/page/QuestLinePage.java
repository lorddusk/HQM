package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.quest.QuestLine;
import hqm.quest.Questbook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public class QuestLinePage implements IPage {

    @Override
    public void init(GuiQuestBook gui) {

    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "questline_page");
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {
        if(side == Side.LEFT){
            Questbook questbook = gui.getQuestbook();
            int y = pageTop;
            for(QuestLine questLine : questbook.getQuestLines()){
                if(y >= pageTop && y <= pageTop + GuiQuestBook.PAGE_HEIGHT){
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(pageLeft, y, 0);
                    gui.mc.fontRenderer.drawString(questLine.getIndex() + ". " + questbook.getName(), 0, 0, 0x000000);
                    GlStateManager.translate(12, 10, 0);
                    GlStateManager.scale(0.75F, 0.75F, 1.0F);
                    gui.mc.fontRenderer.drawString("0% completed", 0, 0, 0x000000);
                    GlStateManager.popMatrix();
                    y += 25;
                }
            }
        }
    }
}
