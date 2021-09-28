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

public class TextBoxGroup {
    
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
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible()) {
                textBox.draw(matrices, selectedTextBox == textBox);
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onClick(int mX, int mY) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible() && textBox.inBounds(mX, mY)) {
                if (selectedTextBox == textBox) {
                    selectedTextBox = null;
                } else {
                    selectedTextBox = textBox;
                }
                break;
            }
        }
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
        protected int offsetY = 3;
        protected final int x;
        protected final int y;
        private int start;
        private String visibleText;
        private final boolean scrollable;
        
        public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable) {
            super(gui, str, scrollable ? Integer.MAX_VALUE : WIDTH, false);
            
            this.x = x;
            this.y = y;
            this.scrollable = scrollable;
        }
        
        @Environment(EnvType.CLIENT)
        protected void draw(PoseStack matrices, boolean selected) {
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec2 mouse = Points.ofMouse();
            this.gui.drawRect(matrices, x, y, TEXT_BOX_SRC_X, TEXT_BOX_SRC_Y + (selected || inBounds(mouse.x - this.gui.getLeft(), mouse.y - this.gui.getTop()) ? TEXT_BOX_HEIGHT : 0), TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
            this.gui.drawString(matrices, scrollable ? visibleText : getText(), x + 3, y + offsetY, getMult(), 0x404040);
            if (selected) {
                this.gui.drawCursor(matrices, x + getCursorPositionX() + 2, y + getCursorPositionY(), 10, 1F, 0xFF909090);
            }
        }
        
        protected boolean isVisible() {
            return true;
        }
        
        @Environment(EnvType.CLIENT)
        @Override
        public void textChanged() {
            super.textChanged();
            if (scrollable) {
                updateVisible();
            }
        }
        
        @Environment(EnvType.CLIENT)
        @Override
        public void recalculateCursor() {
            if (scrollable) {
                if (updatedCursor) {
                    updateVisible();
                    cursorPositionX = (int) (getMult() * this.gui.getStringWidth(visibleText.substring(0, Math.min(visibleText.length(), cursor - start))));
                    updatedCursor = false;
                }
            } else {
                super.recalculateCursor();
            }
        }
        
        @Environment(EnvType.CLIENT)
        private void updateVisible() {
            if (cursor < start) {
                start = cursor;
            }
            
            while (start < cursor) {
                String text = getText().substring(start, cursor);
                if (this.gui.getStringWidth(text) * getMult() > WIDTH) {
                    start++;
                } else {
                    break;
                }
            }
            visibleText = getText().substring(start);
            while (this.gui.getStringWidth(visibleText) * getMult() > WIDTH) {
                visibleText = visibleText.substring(0, visibleText.length() - 2);
            }
        }
        
        @Environment(EnvType.CLIENT)
        public void reloadText() {
            recalculateCursor();
        }
    
        @Environment(EnvType.CLIENT)
        public boolean inBounds(double mX, double mY) {
            return gui.inBounds(x, y, TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT, mX, mY);
        }
    }
}
