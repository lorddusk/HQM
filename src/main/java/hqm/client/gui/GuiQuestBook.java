package hqm.client.gui;

import hqm.HQM;
import hqm.client.gui.page.MainPage;
import hqm.net.Networker;
import hqm.quest.Questbook;
import hqm.quest.SaveHandler;
import hqm.quest.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author canitzp
 */
public class GuiQuestBook extends Screen{

    public static final ResourceLocation LOC_PAGE = new ResourceLocation(HQM.MODID, "textures/gui/book.png");
    public static final ResourceLocation QUESTMAP = new ResourceLocation(HQM.MODID, "textures/gui/questmap.png");
    public static final int PAGE_START_X = 20, PAGE_START_Y = 20, PAGE_WIDTH = 140, PAGE_HEIGHT = 190, PAGE_SECOND_OFFSET = 20;

    private final UUID questbookId;
    public int guiLeft, guiTop, xSize = 340, ySize = 234;
    private double lastX, lastY;
    private ArrayDeque<IPage> lastPages = new ArrayDeque<>();
    private IPage currentPage, rewindButtonPage;
    private List<IRenderer> renderer = new CopyOnWriteArrayList<>();
    private Team team;
    private ItemStack book;
    private PlayerEntity player;

    public GuiQuestBook(UUID questbookId, PlayerEntity player, ItemStack book){
        super(new StringTextComponent("QuestBook"));
        this.questbookId = questbookId;
        this.team = this.getQuestbook().getTeam(player);
        this.book = book;
        this.player = player;
    }

    public Questbook getQuestbook(){
        Questbook questbook = SaveHandler.QUEST_DATA.get(this.questbookId);
        if(questbook == null){
            Minecraft.getInstance().displayGuiScreen(null);
        }
        return questbook;
    }

    public Team getTeam() {
        return team;
    }

    public void tryToLoadPage(ItemStack stack) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!stack.isEmpty() && stack.hasTag()){
            String clazz = stack.getTag().getString("PageClass");
            if(!clazz.isEmpty()){
                Class c = Class.forName(clazz);
                if(c != null && IPage.class.isAssignableFrom(c)){
                    this.setPage((IPage) c.newInstance(), false);
                }
            }
        }
    }

    public void setPage(IPage page, boolean addToLastPageIndex){
        if(addToLastPageIndex && this.currentPage != null){
            this.lastPages.addFirst(this.currentPage);
        }
        if(this.rewindButtonPage == page){
            this.lastPages.clear();
        }
        this.currentPage = page;
        this.renderer.clear();
        this.currentPage.init(this);
    }

    public void setRewindPage(IPage page){
        this.rewindButtonPage = page;
    }

    public void addRenderer(IRenderer renderer){
        this.renderer.add(renderer);
    }

    public void bindTexture(ResourceLocation loc){
        this.getMinecraft().getTextureManager().bindTexture(loc);
    }

    @Override
    public void init() {
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        if(this.currentPage == null){
            this.setPage(new MainPage(), false);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        /* todo when 1.15 mappings are out
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
            renderer1.draw(this, this.guiLeft + PAGE_START_X, this.guiTop + PAGE_START_Y, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, IPage.Side.LEFT);
            renderer1.draw(this, this.guiLeft + PAGE_START_X + PAGE_SECOND_OFFSET + PAGE_WIDTH, this.guiTop + PAGE_START_Y, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, IPage.Side.RIGHT);
        });
        GlStateManager.popMatrix();
        if(!this.lastPages.isEmpty() || this.rewindButtonPage != null){
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(QUESTMAP);
            if(!this.lastPages.isEmpty()){
                boolean isMouseOver = mouseX >= guiLeft + 7 && mouseX <= guiLeft + 7 + 15 && mouseY >= guiTop + 219 && mouseY <= guiTop + 219 + 10;
                this.drawTexturedModalRect(guiLeft + 7, guiTop + 219, isMouseOver ? 15 : 0, 113, 15, 10);
            }
            if(this.rewindButtonPage != null && this.rewindButtonPage != this.currentPage){
                boolean isMouseOver = mouseX >= guiLeft + 162 && mouseX <= guiLeft + 162 + 14 && mouseY >= guiTop + 217 && mouseY <= guiTop + 217 + 9;
                this.drawTexturedModalRect(guiLeft + 162, guiTop + 217, isMouseOver ? 14 : 0, 104, 14, 9);
            }
            GlStateManager.popMatrix();
        }*/
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton){
        //super.mouseClicked(mouseX, mouseY, mouseButton);
        int left = this.guiLeft + PAGE_START_X;
        int top = this.guiTop + PAGE_START_Y;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH + 20 && mouseY >= top && mouseY <= top + PAGE_HEIGHT + 20){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClick(this, finalLeft, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, mouseButton, IPage.Side.LEFT));
        }
        left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH + 20 && mouseY >= top && mouseY <= top + PAGE_HEIGHT + 20){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClick(this, finalLeft, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, mouseButton, IPage.Side.RIGHT));
        }
        if(!this.lastPages.isEmpty() && mouseX >= guiLeft + 7 && mouseX <= guiLeft + 7 + 15 && mouseY >= guiTop + 219 && mouseY <= guiTop + 219 + 10){ // back button
            this.setPage(this.lastPages.removeFirst(), false);
        }
        if(this.rewindButtonPage != null && this.rewindButtonPage != this.currentPage && mouseX >= guiLeft + 162 && mouseX <= guiLeft + 162 + 14 && mouseY >= guiTop + 217 && mouseY <= guiTop + 217 + 9){ // rewind button
            this.setPage(this.rewindButtonPage, false);
        }
        this.lastX = mouseX;
        this.lastY = mouseY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        //super.mouseReleased(mouseX, mouseY, mouseButton);
        int left = this.guiLeft + PAGE_START_X;
        int top = this.guiTop + PAGE_START_Y;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH + 20 && mouseY >= top && mouseY <= top + PAGE_HEIGHT + 20){
            for (IRenderer renderer1 : this.renderer) {
                renderer1.mouseRelease(this, left, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, mouseButton, IPage.Side.LEFT);
            }
        }
        left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH + 20 && mouseY >= top && mouseY <= top + PAGE_HEIGHT + 20){
            for (IRenderer renderer1 : this.renderer) {
                renderer1.mouseRelease(this, left, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, mouseButton, IPage.Side.RIGHT);
            }
        }
        this.lastX = 0;
        this.lastY = 0;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double unknown1, double unknown2) {
        //super.mouseDragged(mouseX, mouseY, mouseButton, unknown1, unknown2);
        int left = this.guiLeft + PAGE_START_X;
        int top = this.guiTop + PAGE_START_Y;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClickMove(this, finalLeft, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, lastX, lastY, mouseButton, IPage.Side.LEFT));
        }
        left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
        if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
            int finalLeft = left;
            this.renderer.forEach(renderer1 -> renderer1.mouseClickMove(this, finalLeft, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, lastX, lastY, mouseButton,  IPage.Side.RIGHT));
        }
        this.lastX = mouseX;
        this.lastY = mouseY;
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount){
        /* todo implement with 1.15 mappings
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
                this.renderer.forEach(renderer1 -> renderer1.mouseScroll(this, finalLeft, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, scroll, IPage.Side.LEFT));
            }
            left += PAGE_SECOND_OFFSET + PAGE_WIDTH;
            if(mouseX >= left && mouseX <= left + PAGE_WIDTH && mouseY >= top && mouseY <= top + PAGE_HEIGHT){
                int finalLeft = left;
                this.renderer.forEach(renderer1 -> renderer1.mouseScroll(this, finalLeft, top, PAGE_WIDTH, PAGE_HEIGHT, mouseX, mouseY, scroll,  IPage.Side.RIGHT));
            }
        }
    }
         */
        return true;
    }
    
    

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onClose(){
        try {
            this.currentPage.getClass().getConstructor();
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("CurrentSlot", this.player.inventory.getSlotFor(this.book));
            nbt.put("Data", Networker.singleTag("PageClass", StringNBT.func_229705_a_(this.currentPage.getClass().getName())));
            // todo 1.15 implement network traffic Networker.NET.sendToServer(new HQMPacket(NetActions.STACK_ADD_NBT, this.player, nbt));
        } catch (NoSuchMethodException ignored) {}
    }
    
}
