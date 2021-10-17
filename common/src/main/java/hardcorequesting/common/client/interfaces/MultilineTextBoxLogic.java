package hardcorequesting.common.client.interfaces;

import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
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
        gui.getFont().getSplitter().splitLines(text, (int) (width / scale), Style.EMPTY, true, (style, start, end) -> {
            String lineText = StringUtils.stripEnd(text.substring(start, end), " \n");
            lines.add(new Line(lineText, start));
        });
        if (text.isEmpty() || text.charAt(text.length() - 1) == '\n')
            lines.add(new Line("", text.length()));
    }
    
    @Override
    public boolean onKeyStroke(int k) {
        if (k == GLFW.GLFW_KEY_UP) {
            changeLine(-1);
            return true;
        } else if (k == GLFW.GLFW_KEY_DOWN) {
            changeLine(1);
            return true;
        } else if (k == GLFW.GLFW_KEY_HOME) {
            setHome();
            return true;
        } else if (k == GLFW.GLFW_KEY_END) {
            setEnd();
            return true;
        } else if (acceptNewlines && (k == GLFW.GLFW_KEY_KP_ENTER || k == GLFW.GLFW_KEY_ENTER)) {
            addText("\n");
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
    
    private void setHome() {
        Line line = lines.get(getLineFor(getCursor()));
        setCursor(line.start());
    }
    
    private void setEnd() {
        Line line = lines.get(getLineFor(getCursor()));
        setCursor(line.start() + line.text().length());
    }
    
    private void changeLine(int direction) {
        int cursor = getCursor();
        int line = getLineFor(cursor);
        int newLineId = line + direction;
        if (0 <= newLineId && newLineId < lines.size()) {
            int offset = getCursor() - lines.get(line).start();
            Line newLine = lines.get(newLineId);
            int newOffset = Math.min(offset, newLine.text().length());
            setCursor(newLine.start() + newOffset);
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