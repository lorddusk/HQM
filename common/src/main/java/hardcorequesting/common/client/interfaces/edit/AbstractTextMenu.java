package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.MultilineTextBoxLogic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;

import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractTextMenu extends GuiEditMenu {
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int LINES_PER_PAGE = 21;
    private static final float TEXT_SCALE = 1F;
    
    protected final MultilineTextBoxLogic textLogic;
    
    protected AbstractTextMenu(GuiQuestBook gui, String text, boolean acceptsNewLines) {
        super(gui, false);
    
        if (text != null && !text.isEmpty()) {
            text = text.replace("\n", "\\n");
        }
        
        this.textLogic = new MultilineTextBoxLogic(gui, Objects.requireNonNullElse(text, ""), 140, TEXT_SCALE, acceptsNewLines);
    
        addClickable(new LargeButton(gui, "hqm.textEditor.copyAll", 185, 20) {
            @Override
            public void onClick() {
                Minecraft.getInstance().keyboardHandler.setClipboard(textLogic.getText());
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.textEditor.paste", 245, 20) {
            @Override
            public void onClick() {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (!clip.isEmpty()) {
                    clip = clip.replace("\n", "\\n");
                }
                textLogic.addText(clip);
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.textEditor.clear", 185, 40) {
            @Override
            public void onClick() {
                textLogic.setTextAndCursor("");
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.textEditor.clearPaste", 245, 40) {
            @Override
            public void onClick() {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (!clip.isEmpty()) {
                    clip = clip.replace("\n", "\\n");
                }
                textLogic.setTextAndCursor(clip);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        int page = textLogic.getCursorLine() / LINES_PER_PAGE;
        gui.drawString(matrices, textLogic.getLines().stream().map(FormattedText::of).collect(Collectors.toList()), page * LINES_PER_PAGE, LINES_PER_PAGE, START_X, START_Y, TEXT_SCALE, 0x404040);
        gui.drawCursor(matrices, START_X + textLogic.getCursorPositionX() - 1, START_Y + textLogic.getCursorPositionY() - 3 - page * LINES_PER_PAGE * GuiBase.TEXT_HEIGHT, 10, TEXT_SCALE, 0xFF909090);
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