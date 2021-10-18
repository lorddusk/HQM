package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.MultilineTextBox;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public abstract class AbstractTextMenu extends GuiEditMenu {
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final float TEXT_SCALE = 1F;
    
    protected final MultilineTextBox textLogic;
    
    protected AbstractTextMenu(GuiQuestBook gui, String text, boolean acceptsNewLines) {
        super(gui, false);
    
        this.textLogic = new MultilineTextBox(gui, START_X, START_Y, Objects.requireNonNullElse(text, ""), 140, TEXT_SCALE, acceptsNewLines);
    
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
                textLogic.setTextAndCursor(clip);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        textLogic.render(matrices, mX, mY);
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