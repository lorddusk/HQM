package hardcorequesting.common.client.interfaces;

import hardcorequesting.common.util.Translator;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.stream.Collectors;

public class MultilineTextBoxLogic extends TextBoxLogic {
    
    private final boolean acceptNewlines;
    
    private List<String> lines;
    private int cursorLine;
    
    public MultilineTextBoxLogic(GuiBase gui, String text, int width, boolean acceptNewlines) {
        super(gui, text, width, Integer.MAX_VALUE);
        this.acceptNewlines = acceptNewlines;
    }
    
    public int getCursorLine() {
        recalculateCursor();
        return cursorLine;
    }
    
    public List<String> getLines() {
        return lines;
    }
    
    @Override
    public void textChanged() {
        lines = this.gui.getLinesFromText(Translator.plain(getText()), getMult(), getWidth()).stream().map(Translator::rawString).collect(Collectors.toList());
    }
    
    @Override
    public boolean onKeyStroke(int k) {
        if (acceptNewlines && (k == GLFW.GLFW_KEY_KP_ENTER || k == GLFW.GLFW_KEY_ENTER)) {
            addText("\\n");
            return true;
        } else return super.onKeyStroke(k);
    }
    
    @Override
    public void recalculateCursor() {
        if (!lines.isEmpty()) {
            if (getAndClearCursorFlag()) {
                int tmpCursor = getCursor();
                for (int i = 0; i < lines.size(); i++) {
                    if (tmpCursor <= lines.get(i).length()) {
                        cursorPositionX = (int) (getMult() * this.gui.getStringWidth(lines.get(i).substring(0, tmpCursor)));
                        cursorPositionY = (int) (GuiBase.TEXT_HEIGHT * i * getMult());
                        cursorLine = i;
                        break;
                    } else {
                        tmpCursor -= lines.get(i).length();
                    }
                }
            }
        } else {
            super.recalculateCursor();
        }
    }
}