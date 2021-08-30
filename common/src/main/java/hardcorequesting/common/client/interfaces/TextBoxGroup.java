package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
    private List<TextBox> textBoxes;
    
    public TextBoxGroup() {
        textBoxes = new ArrayList<>();
    }
    
    public void add(TextBox textBox) {
        textBoxes.add(textBox);
    }
    
    public List<TextBox> getTextBoxes() {
        return textBoxes;
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiBase gui) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible()) {
                textBox.draw(matrices, gui, selectedTextBox == textBox);
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onClick(GuiBase gui, int mX, int mY) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible() && gui.inBounds(textBox.x, textBox.y, TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT, mX, mY)) {
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
    public void onKeyStroke(GuiBase gui, char c, int k) {
        if (selectedTextBox != null && selectedTextBox.isVisible()) {
            selectedTextBox.onKeyStroke(gui, c, k);
        }
    }
    
    public static class TextBox extends TextBoxLogic {
        
        private static final int WIDTH = 60;
        protected int offsetY = 3;
        private int x;
        private int y;
        private int start;
        private String visibleText;
        private boolean scrollable;
        
        public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable) {
            super(gui, str, scrollable ? Integer.MAX_VALUE : WIDTH, false);
            
            this.x = x;
            this.y = y;
            this.scrollable = scrollable;
        }
        
        @Environment(EnvType.CLIENT)
        protected void draw(PoseStack matrices, GuiBase gui, boolean selected) {
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec2 mouse = Points.ofMouse();
            gui.drawRect(x, y, TEXT_BOX_SRC_X, TEXT_BOX_SRC_Y + (selected || gui.inBounds(x, y, TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT, mouse.x - gui.left, mouse.y - gui.top) ? TEXT_BOX_HEIGHT : 0), TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
            gui.drawString(matrices, scrollable ? visibleText : getText(), x + 3, y + offsetY, getMult(), 0x404040);
            if (selected) {
                gui.drawCursor(matrices, x + getCursorPositionX(gui) + 2, y + getCursorPositionY(gui), 10, 1F, 0xFF909090);
            }
        }
        
        protected boolean isVisible() {
            return true;
        }
        
        @Environment(EnvType.CLIENT)
        @Override
        public void textChanged(GuiBase gui) {
            super.textChanged(gui);
            if (scrollable) {
                updateVisible(gui);
            }
        }
        
        @Environment(EnvType.CLIENT)
        @Override
        public void recalculateCursor(GuiBase gui) {
            if (scrollable) {
                if (updatedCursor) {
                    updateVisible(gui);
                    cursorPositionX = (int) (getMult() * gui.getStringWidth(visibleText.substring(0, Math.min(visibleText.length(), cursor - start))));
                    updatedCursor = false;
                }
            } else {
                super.recalculateCursor(gui);
            }
        }
        
        @Environment(EnvType.CLIENT)
        private void updateVisible(GuiBase gui) {
            if (cursor < start) {
                start = cursor;
            }
            
            while (start < cursor) {
                String text = getText().substring(start, cursor);
                if (gui.getStringWidth(text) * getMult() > WIDTH) {
                    start++;
                } else {
                    break;
                }
            }
            visibleText = getText().substring(start);
            while (gui.getStringWidth(visibleText) * getMult() > WIDTH) {
                visibleText = visibleText.substring(0, visibleText.length() - 2);
            }
        }
        
        @Environment(EnvType.CLIENT)
        public void reloadText(GuiBase gui) {
            
        }
    }
}
