package hqm.client.gui;

import hqm.HQM;
import hqm.api.IQuestbook;
import hqm.api.page.ILayout;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiQuestbook extends GuiScreen{
    
    public static final ResourceLocation QUESTBOOK_LOCATION = new ResourceLocation(HQM.MODID, "textures/gui/gui_questbook.png");
    private IQuestbook questbook;
    private int guiLeft;
    private int guiTop;
    private int xSize = 340;
    private int ySize = 234;
    
    private ILayout currentLayout;
    
    public GuiQuestbook(IQuestbook questbook){
        this.questbook = questbook;
    }
    
    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.currentLayout = this.questbook.openBook();
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        GlStateManager.pushMatrix();
        this.bindTexture(QUESTBOOK_LOCATION);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize / 2, this.ySize);
        GlStateManager.translate(this.guiLeft + xSize, this.guiTop + ySize, this.zLevel); // nix touchy pls
        GlStateManager.rotate(180, 0, 0, 1); // nix touchy pls
        this.drawTexturedModalRect(0, 0, 0, 0, this.xSize / 2, this.ySize);
        GlStateManager.popMatrix();
    }
    
    public void bindTexture(ResourceLocation loc){
        this.mc.getTextureManager().bindTexture(loc);
    }
    
}
