package hardcorequesting.common.client.interfaces;

import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultilineTextBoxLogic extends TextBoxLogic {
    
    protected final GuiBase gui;
    private final boolean acceptNewlines;
    private final int width;
    private final float scale;
    
    private final List<Line> lines = new ArrayList<>();
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
        return lines.stream().map(Line::text).collect(Collectors.toList());
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
                (style, start, end) -> lines.add(new Line(text.substring(start, end), start)));
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
            int i = getLineFor(cursor);
            Line line = lines.get(i);
            String text = getText().substring(line.start(), cursor);
            cursorPositionX = (int) (scale * this.gui.getStringWidth(text));
            cursorPositionY = (int) (GuiBase.TEXT_HEIGHT * i * scale);
            cursorLine = i;
        } else {
            cursorPositionX = cursorPositionY = 0;
        }
    }
    
    private int getLineFor(int cursor) {
        for (int i = 0; i < lines.size(); i++) {
            if (cursor < lines.get(i).start)
                return i - 1;
        }
        return lines.size() - 1;
    }
    
    private static record Line(String text, int start) {}
}