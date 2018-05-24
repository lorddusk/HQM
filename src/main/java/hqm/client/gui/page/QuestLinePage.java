package hqm.client.gui.page;

import hqm.HQM;
import hqm.client.Colors;
import hqm.client.gui.GuiQuestBook;
import hqm.client.gui.IPage;
import hqm.client.gui.IRenderer;
import hqm.client.gui.component.ComponentPageOpenButton;
import hqm.client.gui.component.ComponentScrollPane;
import hqm.client.gui.component.ComponentSingleText;
import hqm.client.gui.component.ComponentTextArea;
import hqm.quest.QuestLine;
import hqm.quest.Questbook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author canitzp
 */
public class QuestLinePage implements IPage {

    ComponentScrollPane<QuestLineEntries> scrollPane = new ComponentScrollPane<>(Side.LEFT);

    @Override
    public void init(GuiQuestBook gui) {
        Questbook questbook = gui.getQuestbook();
        for(QuestLine questLine : questbook.getQuestLines()){
            scrollPane.addComponent(new QuestLineEntries(questLine));
        }
        gui.addRenderer(scrollPane);
    }

    @Override
    public void render(GuiQuestBook gui, int pageLeft, int pageTop, int mouseX, int mouseY, Side side) {}

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(HQM.MODID, "questline_page");
    }

    private static class QuestLineEntries implements ComponentScrollPane.IScrollRender{

        private QuestLine questLine;
        private boolean isClicked;
        private ComponentTextArea questLineText;
        private ComponentPageOpenButton openButton;
        private String shortDesc;

        private QuestLineEntries(QuestLine questLine){
            this.questLine = questLine;
            List<ComponentSingleText> text = new ArrayList<>();
            for(String line : questLine.getDescription()){
                if(this.shortDesc == null){
                    this.shortDesc = line;
                    if(this.shortDesc.length() > 20){
                        this.shortDesc = this.shortDesc.substring(0, 19).concat("...");
                    }
                }
                text.addAll(ComponentSingleText.from(Collections.singletonList(line), GuiQuestBook.PAGE_WIDTH, Side.RIGHT));
            }
            text.forEach(componentSingleText -> componentSingleText.setScale(0.95F));
            questLineText = new ComponentTextArea(text, Side.RIGHT);
            openButton = new ComponentPageOpenButton(GuiQuestBook.PAGE_WIDTH / 2, GuiQuestBook.PAGE_HEIGHT + 2, new QuestTreePage(this.questLine), Side.RIGHT);
            openButton.setText(new ComponentSingleText("Open " + questLine.getName(), Side.RIGHT).setColor(Colors.LIGHT_GRAY).setScale(0.85F));
        }

        @Override
        public void render(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, Side side) {
            if(side == Side.LEFT){
                if(this.isClicked){
                    Gui.drawRect(left, top, left + width, top + 1, Colors.LIGHT_GRAY_APLHA);
                    Gui.drawRect(left + width - 1, top, left + width, top + this.getHeight(), Colors.LIGHT_GRAY_APLHA);
                    Gui.drawRect(left, top + this.getHeight() - 1, left + width, top + this.getHeight(), Colors.LIGHT_GRAY_APLHA);
                    Gui.drawRect(left, top, left + 1, top + this.getHeight(), Colors.LIGHT_GRAY_APLHA);
                }
                gui.mc.fontRenderer.drawString(TextFormatting.BOLD + this.questLine.getName() + TextFormatting.RESET, left + 2, top + 2, Colors.BLACK);
                gui.mc.fontRenderer.drawString(TextFormatting.ITALIC + this.shortDesc + TextFormatting.RESET, left + 2, top + 3 + gui.mc.fontRenderer.FONT_HEIGHT, Colors.LIGHT_GRAY);
            }
        }

        @Override
        public void renderRaw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, Side side) {
            if(this.isClicked){
                this.questLineText.draw(gui, left, top, width, height, mouseX, mouseY, side);
                this.openButton.draw(gui, left, top, width, height, mouseX, mouseY, side);
            }
        }

        @Override
        public int getHeight() {
            return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * 2 + 5;
        }

        @Override
        public void click(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, Side side) {
            this.isClicked = true;
        }

        @Override
        public void clickRaw(GuiQuestBook gui, int left, int top, int width, int height, int mouseX, int mouseY, int mouseButton, Side side) {
            if(this.isClicked){
                this.openButton.mouseClick(gui, left, top, width, height, mouseX, mouseY, mouseButton, side);
            }
        }

        @Override
        public void unClicked(GuiQuestBook gui, int mouseX, int mouseY, int mouseButton, Side side) {
            this.isClicked = false;
        }
    }

}
