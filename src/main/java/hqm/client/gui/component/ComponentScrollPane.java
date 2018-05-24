package hqm.client.gui.component;

import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author canitzp
 */
//TODO
public class ComponentScrollPane<T extends ComponentScrollPane.IScrollRender> implements IRenderer{

    public static int WIDTH_BAR = 7;
    public static int OFFSET = 2;
    public static int HEIGHT = 178;

    private int scrollPosition = 0;
    private Map<T, Integer> components = new HashMap<>();
    private IPage.Side side;
    private T currentlyClicked = null;

    public ComponentScrollPane(IPage.Side side){
        this.side = side;
    }

    public void addComponent(T component){
        this.components.put(component, this.components.size() * component.getHeight());
    }

    @Override
    public void draw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side) {
        for(Map.Entry<T, Integer> entry : this.components.entrySet()){
            if(entry.getValue() + this.scrollPosition >= 0 && entry.getValue() + entry.getKey().getHeight() + this.scrollPosition <= height){
                entry.getKey().render(gui, left, top + this.scrollPosition + entry.getValue(), width - WIDTH_BAR - 1, height, mouseX, mouseY, side);
            }
            entry.getKey().renderRaw(gui, left, top, width, height, mouseX, mouseY, side);
        }
        if(this.side == side){
            gui.bindTexture(LOC);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            gui.drawTexturedModalRect(left + width - WIDTH_BAR, top + OFFSET, 171, 69, 7, 187);
            gui.drawTexturedModalRect(left + width - WIDTH_BAR + 1 , top + OFFSET + 1 /*scroll offset calc is missing*/, 250, 167, 5, 6);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void mouseClick(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side) {
        this.components.keySet().forEach(t -> t.clickRaw(gui, left, top, width, height, mouseX, mouseY, mouseButton, side));
        if(this.currentlyClicked != null){
            this.currentlyClicked.unClicked(gui, mouseX, mouseY, mouseButton, side);
        }
        if(this.side == side){
            int lowest = this.getLowestHeight(height);
            int i = ((mouseY - top) / lowest - (this.scrollPosition / lowest)-1) * lowest + lowest;
            for(Map.Entry<T, Integer> entry : this.components.entrySet()) {
                if(entry.getValue() == i){
                    entry.getKey().click(gui, left, top + this.scrollPosition + entry.getValue(), width - WIDTH_BAR - 1, height, mouseX, mouseY, mouseButton, side);
                    this.currentlyClicked = entry.getKey();
                    break;
                }
            }
        }
    }

    @Override
    public void mouseScroll(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int scroll, IPage.Side side) {
        if(this.side == side){
            int lowest = getLowestHeight(height);
            int scrollFactor = (int) (scroll / 120.0F);
            if(scrollFactor > 0 && this.scrollPosition < 0){
                this.scrollPosition = -(-this.scrollPosition - scrollFactor * lowest);
            } else if(scrollFactor < 0 && this.scrollPosition-(lowest*(height/lowest)) > -this.getMaxHeight()){
                this.scrollPosition = -(-this.scrollPosition - scrollFactor * lowest);
            }
        }
    }

    protected int getLowestHeight(int maxHeight){
        int lowest = maxHeight;
        for(T t : this.components.keySet()){
            if(t.getHeight() < lowest){
                lowest = t.getHeight();
            }
        }
        return lowest;
    }

    protected int getMaxHeight(){
        int max = 0;
        for(T t : this.components.keySet()){
            max += t.getHeight();
        }
        return max;
    }

    public  interface IScrollRender{
        void render(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side);
        void renderRaw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, IPage.Side side);
        int getHeight();
        default void click(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side){}
        default void clickRaw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, IPage.Side side){}
        default void unClicked(GuiQuestBook gui, int mouseX, int mouseY, int mouseButton, IPage.Side side){}
    }

}
