package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.TextBoxLogic;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

public class TextBox extends TextBoxLogic {
    private static final int TEXT_BOX_WIDTH = 64;
    private static final int TEXT_BOX_HEIGHT = 12;
    private static final int TEXT_BOX_SRC_X = 192;
    private static final int TEXT_BOX_SRC_Y = 77;
    
    // width used for text
    private static final int WIDTH = TEXT_BOX_WIDTH - 4;
    
    protected final GuiBase gui;
    private final boolean scrollable;
    private final int offsetY;
    protected final int x;
    protected final int y;
    private final float scale;
    
    private int lastCursor = -1;
    private int visibleStart;
    private String visibleText;
    
    public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable) {
        this(gui, str, x, y, scrollable, Integer.MAX_VALUE);
    }
    
    public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable, int charLimit) {
        this(gui, str, x, y, scrollable, charLimit, 1F);
    }
    
    public TextBox(GuiBase gui, String str, int x, int y, boolean scrollable, int charLimit, float scale) {
        super(SharedConstants.filterText(Objects.requireNonNullElse(str, "")), charLimit);
        
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.scrollable = scrollable;
        this.scale = scale;
        this.offsetY = (int) (TEXT_BOX_HEIGHT - scale * GuiBase.TEXT_HEIGHT);
        
        updateVisible();
    }
    
    @Override
    protected boolean isTextValid(String newText) {
        return super.isTextValid(newText) && (scrollable || gui.getStringWidth(newText) * scale <= WIDTH);
    }
    
    @Override
    protected String getStrippedClipboard() {
        return SharedConstants.filterText(super.getStrippedClipboard());
    }
    
    protected void draw(PoseStack matrices, boolean selected, int mX, int mY) {
        checkCursor();
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        this.gui.drawRect(matrices, x, y, TEXT_BOX_SRC_X, TEXT_BOX_SRC_Y + (selected || inBounds(mX, mY) ? TEXT_BOX_HEIGHT : 0), TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT);
        this.gui.drawString(matrices, visibleText, x + 3, y + offsetY, scale, 0x404040);
        
        if (selected) {
            int cursor = getCursor();
            int selection = getSelectionPos();
            
            int cursorPositionX = (int) (scale * this.gui.getStringWidth(visibleText.substring(0, Math.min(visibleText.length(), cursor - visibleStart))));
            this.gui.drawCursor(matrices, x + cursorPositionX + 2, y, 10, 1F, 0xFF909090);
            
            if (cursor != selection) {
                int selectStart = Math.min(cursor, selection);
                int selectEnd = Math.max(cursor, selection);
                gui.drawSelection(matrices, Collections.singleton(getSelectionBox(selectStart, selectEnd)));
            }
        }
    }
    
    @NotNull
    private Rect2i getSelectionBox(int selectStart, int selectEnd) {
        selectStart = Math.max(visibleStart, selectStart);
        selectEnd = Math.min(visibleStart + visibleText.length(), selectEnd);
        return new Rect2i(x + 3 + (int) (scale * gui.getStringWidth(getText().substring(visibleStart, selectStart))), y + offsetY - 1,
                (int) (scale * gui.getStringWidth(getText().substring(selectStart, selectEnd))), (int) (scale * GuiBase.TEXT_HEIGHT));
    }
    
    protected boolean isVisible() {
        return true;
    }
    
    @Override
    public void textChanged() {
        updateVisible();
    }
    
    public void checkCursor() {
        if (lastCursor != getCursor()) {
            lastCursor = getCursor();
            updateVisible();
        }
    }
    
    private void updateVisible() {
        if (scrollable) {
            int visibleStart = this.visibleStart;
            
            // Move the visible area if the cursor is too far to the left
            if (getCursor() < visibleStart) {
                visibleStart = getCursor();
            }
            
            // Move the visible area if the cursor is too far to the right
            while (visibleStart < getCursor()) {
                String text = getText().substring(visibleStart, getCursor());
                if (this.gui.getStringWidth(text) * scale > WIDTH)
                    visibleStart++;
                else
                    break;
            }
            
            // Move the visible area if there is unseen text to the left and unused space to the right
            while (0 < visibleStart) {
                String text = getText().substring(visibleStart - 1);
                if (this.gui.getStringWidth(text) * scale <= WIDTH)
                    visibleStart--;
                else
                    break;
            }
            
            setVisibleStart(visibleStart);
        } else {
            setVisibleStart(0);
        }
    }
    
    private void setVisibleStart(int visibleStart) {
        this.visibleStart = visibleStart;
        this.visibleText = gui.getFont().getSplitter().formattedHeadByWidth(getText().substring(visibleStart), (int) (WIDTH / scale), Style.EMPTY);
    }
    
    public void reloadText() {
        checkCursor();
    }
    
    public boolean inBounds(double mX, double mY) {
        return gui.inBounds(x, y, TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT, mX, mY);
    }
}
