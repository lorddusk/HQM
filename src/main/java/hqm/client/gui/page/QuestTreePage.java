package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import hqm.quest.Quest;
import hqm.quest.QuestLine;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public class QuestTreePage implements IPage, IRenderer {

    private static final ResourceLocation questLoc = new ResourceLocation(HQM.MODID, "textures/gui/questmap.png");
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
        gui.addRenderer(this);
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, double mouseX, double mouseY, Side side) {
        if(side == Side.LEFT){
            for(Quest quest : questLine.getQuests()){
                /*
                GlStateManager.pushMatrix();
                gui.mc.getTextureManager().bindTexture(questLoc);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                renderQuest(gui, quest, pageLeft, pageTop);
                GlStateManager.popMatrix();
                
                 */
            }
        }
    }

    private void renderQuest(GuiQuestBook gui, Quest quest, int left, int top){
        if(quest.isInvisible(this.questLine, gui.getTeam())){
            return;
        }
        int x = quest.posX + left;
        int y = quest.posY + top;
        boolean isDone = quest.isDone(gui.getTeam());
        /* todo 1.15 mappings
        if(!quest.isOpen(this.questLine, gui.getTeam())){
            GlStateManager.color(0.5F, 0.5F, 0.5F, 0.5F);
        }
        gui.drawTexturedModalRect(x, y, quest.isBig() ? 195 : 171, quest.isBig() ? (isDone ? 1 : 38) : (isDone ? 2 : 32), quest.isBig() ? 31 : 23, quest.isBig() ? 36 : 26);
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(0.95F, 0.95F, 1.0F);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(quest.getIcon(), quest.isBig() ? 9 : 4, quest.isBig() ? 9 : 4);
       
         */
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, Side side) {}

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, double mouseX, double mouseY, int mouseButton, Side side) {
        for (Quest quest : questLine.getQuests()) {
            if (quest.posX + left <= mouseX && quest.posX + left + (quest.isBig() ? 31 : 23) >= mouseX) {
                if (quest.posY + top <= mouseY && quest.posY + top + (quest.isBig() ? 36 : 26) >= mouseY) {
                    if (!quest.isInvisible(this.questLine, gui.getTeam()) && quest.isOpen(this.questLine, gui.getTeam())) {
                        gui.setPage(new QuestPage(this.questLine, quest), true);
                    }
                }
            }
        }
    }

    @Override
    public void setOffset(int x, int y) {}

}
