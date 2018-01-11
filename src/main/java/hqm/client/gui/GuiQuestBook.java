package hqm.client.gui;

import hqm.HQM;
import hqm.client.gui.page.DebugPage;
import hqm.client.gui.page.MainPage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * @author canitzp
 */
public class GuiQuestBook extends GuiScreen {

    public static final ResourceLocation LOC_PAGE = new ResourceLocation(HQM.MODID, "textures/gui/book.png");
    public static final int PAGE_START_X = 20, PAGE_START_Y = 20, PAGE_WIDTH = 140, PAGE_HEIGHT = 190, PAGE_SECOND_OFFSET = 20;

    public int guiLeft, guiTop, xSize = 340, ySize = 234;
    private int lastX, lastY;
    private IPage currentPage;
    private NonNullList<IRenderer> renderer = NonNullList.create();

    public void tryToLoadPage(NBTTagCompound nbt){
        // TODO load the current page from the item stack nbt. pages does have identifier to use
    }

    public void setPage(IPage page){
        this.currentPage = page;
    }

    public void addRenderer(IRenderer renderer){
        this.renderer.add(renderer);
    }

    public void bindTexture(ResourceLocation loc){
        this.mc.getTextureManager().bindTexture(loc);
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        if(this.currentPage == null){
            //this.setPage(new MainPage());
            this.setPage(new DebugPage());
        }
        this.currentPage.init(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        this.bindTexture(LOC_PAGE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize / 2, ySize);
        GlStateManager.translate(this.guiLeft + xSize, this.guiTop + ySize, this.zLevel); // nix touchy pls
        GlStateManager.rotate(180, 0, 0, 1); // nix touchy pls
        this.drawTexturedModalRect(0, 0, 0, 0, xSize / 2, ySize); // nix touchy pls
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.currentPage.render(this, this.guiLeft + PAGE_START_X, this.guiTop + PAGE_START_Y, mouseX, mouseY, IPage.Side.LEFT);
        this.currentPage.render(this, this.guiLeft + PAGE_START_X + PAGE_SECOND_OFFSET + PAGE_WIDTH, this.guiTop + PAGE_START_Y, mouseX, mouseY, IPage.Side.RIGHT);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.renderer.forEach(renderer1 -> {
            renderer1.draw(this, this.guiLeft + PAGE_START_X, this.guiTop + PAGE_START_Y, mouseX, mouseY, IPage.Side.LEFT);
            renderer1.draw(this, this.guiLeft + PAGE_START_X + PAGE_SECOND_OFFSET + PAGE_WIDTH, this.guiTop + PAGE_START_Y, mouseX, mouseY, IPage.Side.RIGHT);
        });
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int left = this.guiLeft + PAGE_START_X;
        int top = this.guiTop + PAGE_START_Y;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClick(this, finalLeft, top, mouseX, mouseY, mouseButton, IPage.Side.LEFT));
        }
        left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClick(this, finalLeft, top, mouseX, mouseY, mouseButton, IPage.Side.RIGHT));
        }
        this.lastX = mouseX;
        this.lastY = mouseY;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        int left = this.guiLeft + PAGE_START_X;
        int top = this.guiTop + PAGE_START_Y;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseRelease(this, finalLeft, top, mouseX, mouseY, mouseButton, IPage.Side.LEFT));
        }
        left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseRelease(this, finalLeft, top, mouseX, mouseY, mouseButton, IPage.Side.RIGHT));
        }
        this.lastX = 0;
        this.lastY = 0;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        int left = this.guiLeft + PAGE_START_X;
        int top = this.guiTop + PAGE_START_Y;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClickMove(this, finalLeft, top, mouseX, mouseY, lastX, lastY, mouseButton, timeSinceLastClick, IPage.Side.LEFT));
        }
        left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClickMove(this, finalLeft, top, mouseX, mouseY, lastX, lastY, mouseButton, timeSinceLastClick,  IPage.Side.RIGHT));
        }
        this.lastX = mouseX;
        this.lastY = mouseY;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = (Mouse.getEventX() * this.width / this.mc.displayWidth);
        int mouseY = (this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1);
        int scroll = Mouse.getEventDWheel();
        if(scroll != 0){
            int left = this.guiLeft + PAGE_START_X;
            int top = this.guiTop + PAGE_START_Y;
            if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
                int finalLeft = left;
                this.renderer.forEach(renderer1 -> renderer1.mouseScroll(this, finalLeft, top, mouseX, mouseY, scroll, IPage.Side.LEFT));
            }
            left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
            if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
                int finalLeft = left;
                this.renderer.forEach(renderer1 -> renderer1.mouseScroll(this, finalLeft, top, mouseX, mouseY, scroll,  IPage.Side.RIGHT));
            }
        }
    }
}
