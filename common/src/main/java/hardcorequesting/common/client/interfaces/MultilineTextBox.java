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
    
    private final List<Line> lines = new ArrayList<>();
    
    public MultilineTextBox(GuiBase gui, int x, int y, @NotNull String text, int width, boolean acceptNewlines) {
        super(acceptNewlines ? text : text.replace("\n", ""), Integer.MAX_VALUE);
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.acceptNewlines = acceptNewlines;
        this.width = width;
        
        initLines();
    }
    
    @Override
    public void render(PoseStack matrices, int mX, int mY) {
        checkCursor();
        int cursor = getCursor();
        int selection = getSelectionPos();
        int cursorLine = getLineFor(cursor);
        int pageStartLine = cursorLine - (cursorLine % LINES_PER_PAGE);
        
        gui.drawString(matrices, lines.stream().skip(pageStartLine).limit(LINES_PER_PAGE).map(Line::text).map(FormattedText::of).collect(Collectors.toList()), x, y, 1F, 0x404040);
        gui.drawCursor(matrices, x + getTextX(cursorLine, cursor) - 1, y + getTextY(cursorLine, pageStartLine) - 3, 10, 1F, 0xFF909090);
        
        if (cursor != selection) {
            int selectStart = Math.min(cursor, selection);
            int selectEnd = Math.max(cursor, selection);
            gui.drawSelection(matrices, calculateSelectionBoxes(selectStart, selectEnd, pageStartLine));
        }
    }
    
    @NotNull
    private List<Rect2i> calculateSelectionBoxes(int selectStart, int selectEnd, int pageStartLine) {
        List<Rect2i> selections = new ArrayList<>();
        int startLine = getLineFor(selectStart), endLine = getLineFor(selectEnd);
        
        for (int lineId = startLine; lineId <= endLine; lineId++) {
            if (isLineVisible(lineId, pageStartLine)) {
                Line line = lines.get(lineId);
                int lineStart = lineId == startLine ? selectStart : line.start();
                int lineEnd = lineId == endLine ? selectEnd : line.end();

                selections.add(new Rect2i(x + getTextX(lineId, lineStart), y + getTextY(lineId, pageStartLine) - 1,
                        gui.getStringWidth(getText().substring(lineStart, lineEnd)), GuiBase.TEXT_HEIGHT));
            }
        }
        return selections;
    }
    
    private boolean isLineVisible(int line, int pageStartLine) {
        return pageStartLine <= line && line < pageStartLine + LINES_PER_PAGE;
    }
    
    private int getTextX(int line, int cursor) {
        return this.gui.getStringWidth(getText().substring(lines.get(line).start(), cursor));
    }
    
    private int getTextY(int line, int pageStartLine) {
        return (line - pageStartLine) * GuiBase.TEXT_HEIGHT;
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
    
    private void setHome() {
        Line line = lines.get(getLineFor(getCursor()));
        setCursor(line.start());
    }
    
    private void setEnd() {
        Line line = lines.get(getLineFor(getCursor()));
        setCursor(line.start() + line.text().length());
    }
    
    @Override
    protected void recalculateCursorDetails(int cursor) {
    }
    
    @Override
    public void textChanged() {
        initLines();
    }
    
    private void initLines() {
        lines.clear();
        
        String text = getText();
        gui.getFont().getSplitter().splitLines(text, width, Style.EMPTY, true, (style, start, end) -> {
            String lineText = StringUtils.stripEnd(text.substring(start, end), " \n");
            lines.add(new Line(lineText, start, end));
        });
        if (text.isEmpty() || text.charAt(text.length() - 1) == '\n')
            lines.add(new Line("", text.length(), text.length()));
    }
    
    private int getLineFor(int cursor) {
        for (int i = 0; i < lines.size(); i++) {
            if (cursor < lines.get(i).start)
                return i - 1;
        }
        return lines.size() - 1;
    }
    
    private static record Line(String text, int start, int end) {}
}