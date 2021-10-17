package hardcorequesting.common.client.interfaces;

import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MultilineTextBoxLogic extends TextBoxLogic {
    
    protected final GuiBase gui;
    private final boolean acceptNewlines;
    private final int width;
    private final float scale;
    
    private final List<String> lines = new ArrayList<>();
    private int cursorLine;
    private int cursorPositionX, cursorPositionY;
    
    public MultilineTextBoxLogic(GuiBase gui, @NotNull String text, int width, float scale, boolean acceptNewlines) {
        super(acceptNewlines ? text : text.replace("\n", ""), Integer.MAX_VALUE);
        this.gui = gui;
        this.acceptNewlines = acceptNewlines;
        this.width = width;
        this.scale = scale;
        
        initLines();
    }
    
    public int getCursorLine() {
        checkCursor();
        return cursorLine;
    }
    
    public List<String> getLines() {
        return lines;
    }
    
    public int getCursorPositionX() {
        checkCursor();
        return cursorPositionX;
    }
    
    public int getCursorPositionY() {
        checkCursor();
        return cursorPositionY;
    }
    
    @Override
    public void textChanged() {
        initLines();
    }
    
    private void initLines() {
        lines.clear();
    
        String text = getText();
        gui.getFont().getSplitter().splitLines(text, (int) (width / scale), Style.EMPTY, true,
                (style, start, end) -> lines.add(text.substring(start, end)));
    }
    
    @Override
    public boolean onKeyStroke(int k) {
        if (acceptNewlines && (k == GLFW.GLFW_KEY_KP_ENTER || k == GLFW.GLFW_KEY_ENTER)) {
            addText("\\n");
            return true;
        } else return super.onKeyStroke(k);
    }
    
    @Override
    protected void recalculateCursorDetails(int cursor) {
        if (!lines.isEmpty()) {
            int tmpCursor = cursor;
            for (int i = 0; i < lines.size(); i++) {
                if (tmpCursor <= lines.get(i).length()) {
                    cursorPositionX = (int) (scale * this.gui.getStringWidth(lines.get(i).substring(0, tmpCursor)));
                    cursorPositionY = (int) (GuiBase.TEXT_HEIGHT * i * scale);
                    cursorLine = i;
                    break;
                } else {
                    tmpCursor -= lines.get(i).length();
                }
            }
        } else {
            cursorPositionX = cursorPositionY = 0;
        }
    }
}