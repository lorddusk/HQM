package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.MultilineTextBox;

import java.util.Objects;

public abstract class AbstractTextMenu extends GuiEditMenu {
    private static final int START_X = 20;
    private static final int START_Y = 20;
    
    protected final MultilineTextBox textLogic;
    
    protected AbstractTextMenu(GuiQuestBook gui, String text, boolean acceptsNewLines) {
        super(gui, false);
    
        addClickable(this.textLogic = new MultilineTextBox(gui, START_X, START_Y, Objects.requireNonNullElse(text, ""), 140, acceptsNewLines));
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        return textLogic.onKeyStroke(keyCode) || super.keyPressed(keyCode);
    }
    
    @Override
    public boolean charTyped(char c) {
        return textLogic.onCharTyped(c) || super.charTyped(c);
    }
}