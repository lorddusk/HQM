package hardcorequesting.client.interfaces;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.config.ModConfig;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;


public class TextBoxGroup {

    private static final int TEXT_BOX_WIDTH = 64;
    private static final int TEXT_BOX_HEIGHT = 12;
    private static final int TEXT_BOX_SRC_X = 192;
    private static final int TEXT_BOX_SRC_Y = 77;

    private TextBox selectedTextBox;
    List<TextBox> textBoxes;

    public TextBoxGroup() {
        textBoxes = new ArrayList<TextBox>();
    }

    public void add(TextBox textBox) {
        textBoxes.add(textBox);
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiBase gui) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible()) {
                textBox.draw(gui, selectedTextBox == textBox);
            }
        }
    }

    @SideOnly(Side.CLIENT)
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

    @SideOnly(Side.CLIENT)
    public void onKeyStroke(GuiBase gui, char c, int k) {
        if (selectedTextBox != null && selectedTextBox.isVisible()) {
            selectedTextBox.onKeyStroke(gui, c, k);
        }
    }

    public static class TextBox extends TextBoxLogic {
        private int x;
        private int y;
        protected int offsetY = 3;
        private int start;
        private String visibleText;
        private static final int WIDTH = 60;
        private boolean scrollable;

        public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable) {
            super(gui, str, scrollable ? Integer.MAX_VALUE : WIDTH, false);

            this.x = x;
            this.y = y;
            this.scrollable = scrollable;
        }

        @SideOnly(Side.CLIENT)
        protected void draw(GuiBase gui, boolean selected) {

            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            GL11.glColor4f(1F, 1F, 1F, 1F);
            gui.drawRect(x, y, TEXT_BOX_SRC_X, TEXT_BOX_SRC_Y + (selected ? TEXT_BOX_HEIGHT : 0), TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
            gui.drawString(scrollable ? visibleText : getText(), x + 3, y + offsetY, getMult(), 0x404040);
            if (selected) {
                gui.drawCursor(x + getCursorPositionX(gui) + 2, y + getCursorPositionY(gui), 10, 1F, 0xFF909090);
            }
        }

        protected boolean isVisible() {
            return true;
        }


        @SideOnly(Side.CLIENT)
        @Override
        protected void recalculateCursor(GuiBase gui) {
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

        @SideOnly(Side.CLIENT)
        @Override
        protected void textChanged(GuiBase gui) {
            super.textChanged(gui);
            if (scrollable) {
                updateVisible(gui);
            }
        }

        @SideOnly(Side.CLIENT)
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

        @SideOnly(Side.CLIENT)
        public void reloadText(GuiBase gui) {

        }
    }
}
