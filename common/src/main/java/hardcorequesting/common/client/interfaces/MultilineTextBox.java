package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.widget.Drawable;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultilineTextBox extends TextBoxLogic implements Drawable {
    private static final int LINES_PER_PAGE = 21;
    
    private final GuiBase gui;
    private final int x, y;
    private final boolean acceptNewlines;
    private final int width;
    private final float scale;
    
    private final List<Line> lines = new ArrayList<>();
    private int cursorLine;
    private int cursorPositionX, cursorPositionY;
    
    public MultilineTextBox(GuiBase gui, int x, int y, @NotNull String text, int width, float scale, boolean acceptNewlines) {
        super(acceptNewlines ? text : text.replace("\n", ""), Integer.MAX_VALUE);
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.acceptNewlines = acceptNewlines;
        this.width = width;
        this.scale = scale;
        
        initLines();
    }
    
    private int getCursorLine() {
        checkCursor();
        return cursorLine;
    }
    
    private int getCursorPositionX() {
        checkCursor();
        return cursorPositionX;
    }
    
    private int getCursorPositionY() {
        checkCursor();
        return cursorPositionY;
    }
    
    @Override
    public void render(PoseStack matrices, int mX, int mY) {
        int pageStart = getCursorLine() - (getCursorLine() % LINES_PER_PAGE);
        gui.drawString(matrices, lines.stream().map(Line::text).map(FormattedText::of).collect(Collectors.toList()), pageStart, LINES_PER_PAGE, x, y, scale, 0x404040);
        gui.drawCursor(matrices, x + getCursorPositionX() - 1, y + getCursorPositionY() - 3 - (int) (pageStart * GuiBase.TEXT_HEIGHT * scale), 10, scale, 0xFF909090);
        int cursor = getCursor();
        int selection = getSelectionPos();
        if (cursor != selection) {
            int selectStart = Math.min(cursor, selection);
            int selectEnd = Math.max(cursor, selection);
            int startLine = getLineFor(selectStart), endLine = getLineFor(selectEnd);
            for (int lineId = startLine; lineId <= endLine; lineId++) {
                if (pageStart <= lineId && lineId < pageStart + LINES_PER_PAGE) {
                    Line line = lines.get(lineId);
                    int lineStart = lineId == startLine ? selectStart : line.start();
                    int lineEnd = lineId == endLine ? selectEnd : lines.get(lineId + 1).start();
                    int lineY = (int) (GuiBase.TEXT_HEIGHT * (lineId - pageStart) * scale);
                    Rect2i selectionSpace = new Rect2i(x + (int) (scale * gui.getStringWidth(getText().substring(line.start(), lineStart))),
                            y - 1 + lineY, (int) (scale * gui.getStringWidth(getText().substring(lineStart, lineEnd))), (int) (scale * GuiBase.TEXT_HEIGHT));
                    gui.drawSelection(matrices, selectionSpace);
                }
            }
        }
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