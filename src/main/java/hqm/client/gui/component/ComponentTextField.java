package hqm.client.gui.component;

import hqm.HQM;
import hqm.client.gui.AbstractRender;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author canitzp
 */
public class ComponentTextField extends AbstractRender {

    private List<String> text;
    private boolean isSingle;

    public ComponentTextField(boolean isOneLine){
        this.text = isOneLine ? Collections.singletonList(null) : new ArrayList<>();
        this.isSingle = isOneLine;
    }

    public ComponentTextField setText(List<String> text){
        if(text.size() > 0){
            if(this.isSingle){
                this.text.set(0, text.get(0));
            } else {
                this.text.clear();
                this.text.addAll(text);
            }
        }
        return this;
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {

    }

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {

    }

    @Override
    public void mouseRelease(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {

    }

    @Override
    public void mouseClickMove(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int lastMouseX, int lastMouseY, int mouseButton, long ticks, IPage.Side side) {

    }

    @Override
    public void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, IPage.Side side) {

    }

}
