package hardcorequesting.common.client.interfaces;


import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class GuiWrapperEditMenu extends GuiBase {
    
    public static final ResourceLocation BG_TEXTURE = ResourceHelper.getResource("wrapper");
    public static final ResourceLocation C_BG_TEXTURE = ResourceHelper.getResource("c_wrapper");
    private static final int TEXTURE_WIDTH = 170;
    private static final int TEXTURE_HEIGHT = 234;
    private GuiEditMenu editMenu;
    
    public GuiWrapperEditMenu() {
        super(NarratorChatListener.NO_TITLE);
    }
    
    @Override
    public void setEditMenu(GuiEditMenu editMenu) {
        if (editMenu != null) {
            this.editMenu = editMenu;
        } else {
            this.minecraft.setScreen(null);
        }
    }
    
    @Override
    public void render(PoseStack matrices, int mX0, int mY0, float f) {
        boolean doublePage = editMenu.doesRequiredDoublePage();
        
        this.left = (this.width - (doublePage ? TEXTURE_WIDTH * 2 : TEXTURE_WIDTH)) / 2;
        this.top = (this.height - TEXTURE_HEIGHT) / 2;
        
        applyColor(0xFFFFFFFF);
        
        ResourceHelper.bindResource(BG_TEXTURE);
        
        
        drawRect(matrices, 0, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (doublePage) {
            drawRect(matrices, TEXTURE_WIDTH, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, RenderRotation.FLIP_HORIZONTAL);
        }
        
        applyColor(0xFFFFFFFF);
        
        ResourceHelper.bindResource(MAP_TEXTURE);
        
        
        int mX = mX0 - left;
        int mY = mY0 - top;
        
        if (editMenu != null) {
            editMenu.drawFull(matrices, mX, mY);
        }
    }
    
    @Override
    public boolean charTyped(char c, int k) {
        if (super.charTyped(c, k)) {
            return true;
        }
        
        if (editMenu != null) {
            editMenu.charTyped(c);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        if (editMenu != null) {
            editMenu.keyPressed(keyCode);
        }
        return false;
    }
    
    @Override
    public boolean mouseClicked(double mX0, double mY0, int b) {
        int mX = (int) (mX0 - left);
        int mY = (int) (mY0 - top);
        
        if (editMenu != null) {
            editMenu.onClick(mX, mY, b);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mX0, double mY0, int b) {
        int mX = (int) (mX0 - left);
        int mY = (int) (mY0 - top);
        
        if (editMenu != null) {
            editMenu.onRelease(mX, mY, b);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int mX = (int) (mouseX - left);
        int mY = (int) (mouseY - top);
        
        if (editMenu != null) {
            editMenu.onDrag(mX, mY, button);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double d, double e, double amount) {
        if (super.mouseScrolled(d, e, amount)) {
            return true;
        }
        editMenu.onScroll(d, e, amount);
        return true;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
}
