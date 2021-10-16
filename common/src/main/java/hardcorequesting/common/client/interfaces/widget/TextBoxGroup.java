package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.TextBoxLogic;
import hardcorequesting.common.util.Points;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class TextBoxGroup implements Drawable, Clickable {
    
    private static final int TEXT_BOX_WIDTH = 64;
    private static final int TEXT_BOX_HEIGHT = 12;
    private static final int TEXT_BOX_SRC_X = 192;
    private static final int TEXT_BOX_SRC_Y = 77;
    
    private TextBox selectedTextBox;
    private final List<TextBox> textBoxes = new ArrayList<>();
    
    public void add(TextBox textBox) {
        textBoxes.add(textBox);
    }
    
    public List<TextBox> getTextBoxes() {
        return textBoxes;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void render(PoseStack matrices, int mX, int mY) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible()) {
                textBox.draw(matrices, selectedTextBox == textBox);
            }
        }
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public boolean onClick(int mX, int mY) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible() && textBox.inBounds(mX, mY)) {
                if (selectedTextBox == textBox) {
                    selectedTextBox = null;
                } else {
                    selectedTextBox = textBox;
                }
                return true;
            }
        }
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean onKeyStroke(int k) {
        if (selectedTextBox != null && selectedTextBox.isVisible()) {
            return selectedTextBox.onKeyStroke(k);
        }
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean onCharTyped(char c) {
        if (selectedTextBox != null && selectedTextBox.isVisible()) {
            return selectedTextBox.onCharTyped(c);
        }
        return false;
    }
    
    public static class TextBox extends TextBoxLogic {
        
        private static final int WIDTH = 60;
        
        private final boolean scrollable;
        private int width;
        protected int offsetY = 3;
        protected final int x;
        protected final int y;
        private float scale = 1F;
        
        private int start;
        private String visibleText;
        private int cursorPositionX;
        
        public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable) {
            this(gui, str, x, y, scrollable, Integer.MAX_VALUE);
        }
        
        public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable, int charLimit) {
            super(gui, str, charLimit);
            
            this.x = x;
            this.y = y;
            this.width = scrollable ? Integer.MAX_VALUE : WIDTH;
            this.scrollable = scrollable;
        }
    
        public void setWidth(int width) {
            this.width = width;
        }
    
        public void setScale(float scale) {
            this.scale = scale;
        }
        
        @Override
        protected boolean isTextValid(String newText) {
            return super.isTextValid(newText) && gui.getStringWidth(newText) * scale <= width;
        }
    
        @Environment(EnvType.CLIENT)
        protected void draw(PoseStack matrices, boolean selected) {
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec2 mouse = Points.ofMouse();
            this.gui.drawRect(matrices, x, y, TEXT_BOX_SRC_X, TEXT_BOX_SRC_Y + (selected || inBounds(mouse.x - this.gui.getLeft(), mouse.y - this.gui.getTop()) ? TEXT_BOX_HEIGHT : 0), TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
            this.gui.drawString(matrices, scrollable ? visibleText : getText(), x + 3, y + offsetY, scale, 0x404040);
            if (selected) {
                this.gui.drawCursor(matrices, x + cursorPositionX + 2, y, 10, 1F, 0xFF909090);
            }
        }
        
        protected boolean isVisible() {
            return true;
        }
        
        @Environment(EnvType.CLIENT)
        @Override
        public void textChanged() {
            if (scrollable) {
                updateVisible();
            }
        }
        
        @Environment(EnvType.CLIENT)
        @Override
        protected void recalculateCursorDetails(int cursor) {
            if (scrollable) {
                updateVisible();
                cursorPositionX = (int) (scale * this.gui.getStringWidth(visibleText.substring(0, Math.min(visibleText.length(), cursor - start))));
            } else {
                cursorPositionX = (int) (scale * this.gui.getStringWidth(getText().substring(0, cursor)));
            }
        }
        
        @Environment(EnvType.CLIENT)
        private void updateVisible() {
            if (getCursor() < start) {
                start = getCursor();
            }
            
            while (start < getCursor()) {
                String text = getText().substring(start, getCursor());
                if (this.gui.getStringWidth(text) * scale > WIDTH) {
                    start++;
                } else {
                    break;
                }
            }
            visibleText = getText().substring(start);
            while (this.gui.getStringWidth(visibleText) * scale > WIDTH) {
                visibleText = visibleText.substring(0, visibleText.length() - 2);
            }
        }
        
        @Environment(EnvType.CLIENT)
        public void reloadText() {
            checkCursor();
        }
    
        @Environment(EnvType.CLIENT)
        public boolean inBounds(double mX, double mY) {
            return gui.inBounds(x, y, TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT, mX, mY);
        }
    }
}
