package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.ComponentCollector;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class GuiEditMenuCommandEditor extends GuiEditMenuTextEditor {
    
    private Quest quest;
    private int id;
    private String[] commands;
    private boolean[] edited;
    private String added;
    
    public GuiEditMenuCommandEditor(GuiQuestBook gui, Player player) {
        super(gui, player, "", false);
        this.quest = GuiQuestBook.selectedQuest;
        this.commands = this.quest.getCommandRewardsAsStrings();
        this.edited = new boolean[this.commands.length];
        this.id = -1;
        if (gui.getCurrentMode() == EditMode.COMMAND_CHANGE) {
            if (this.commands.length > 0) {
                this.id = this.commands.length - 1;
                this.text.setTextAndCursor(gui, this.commands[this.id]);
            }
        }
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (this.commands[i].isEmpty()) {
                    drawStringTrimmed(matrices, gui, Translator.translatable("hqm.commandEdit.deleted"), 190, 65 + (i * 10), 0xFF0000);
                } else {
                    drawStringTrimmed(matrices, gui, Translator.plain(this.commands[i]), 190, 65 + (i * 10), edited[i] ? 0xFF4500 : 0x000000);
                }
            }
        }
        if (this.added != null && !this.added.isEmpty()) {
            drawStringTrimmed(matrices, gui, Translator.plain(this.added), 190, 65 + (i * 10), 0x447449);
        }
    }
    
    @Override
    public void save(GuiBase gui) {
        if (this.id < 0) this.added = this.text.getText();
        else if (this.commands != null) {
            if (!this.commands[this.id].equals(this.text.getText())) {
                this.edited[this.id] = true;
                this.commands[this.id] = this.text.getText();
            }
        }
        
        if (this.added != null && !this.added.isEmpty()) {
            quest.addCommand(this.added);
            SaveHelper.add(SaveHelper.EditType.COMMAND_ADD);
        }
        if (this.commands != null) {
            for (int i = this.commands.length - 1; i >= 0; i--) {
                if (edited[i]) {
                    if (commands[i].isEmpty()) {
                        quest.removeCommand(i);
                        SaveHelper.add(SaveHelper.EditType.COMMAND_REMOVE);
                    } else {
                        quest.editCommand(i, this.commands[i]);
                        SaveHelper.add(SaveHelper.EditType.COMMAND_CHANGE);
                    }
                }
            }
        }
    }
    
    @Override
    public void renderTooltip(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.renderTooltip(matrices, gui, mX, mY);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (mX > 190 && mX < 300 && mY > 65 + (i * 10) && mY < 65 + ((i + 1) * 10)) {
                    if (this.commands[i].isEmpty()) {
                        drawStringTrimmed(matrices, gui, Translator.translatable("hqm.commandEdit.deleted"), 190, 65 + (i * 10), 0xF76767);
                    } else {
                        drawStringTrimmed(matrices, gui, Translator.plain(this.commands[i]), 190, 65 + (i * 10), edited[i] ? 0xF9AB7A : 0x969696);
                    }
                }
            }
        }
        if (this.added != null && !this.added.isEmpty()) {
            drawStringTrimmed(matrices, gui, Translator.plain(this.added), 190, 65 + (i * 10), 0x5A9B60);
        }
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (mX > 190 && mX < 300 && mY > 65 + (i * 10) && mY < 65 + ((i + 1) * 10)) {
                    if (this.id == i) return;
                    if (this.id < 0) this.added = this.text.getText();
                    else {
                        if (!this.commands[this.id].equals(this.text.getText())) {
                            this.edited[this.id] = true;
                            this.commands[this.id] = this.text.getText();
                        }
                    }
                    this.id = i;
                    this.text.setTextAndCursor(gui, this.commands[this.id]);
                }
            }
        }
        if (mX > 190 && mX < 300 && mY > 65 + (i * 10) && mY < 65 + ((i + 1) * 10)) {
            if (this.id == -1) return;
            if (this.commands != null) this.commands[this.id] = this.text.getText();
            this.id = -1;
            this.text.setTextAndCursor(gui, this.added);
        }
    }
    
    private void drawStringTrimmed(PoseStack matrices, GuiBase gui, FormattedText text, int x, int y, int colour) {
        CharacterLimitingVisitor characterLimitingVisitor = new CharacterLimitingVisitor(25);
        text = text.visit(new FormattedText.StyledContentConsumer<FormattedText>() {
            private final ComponentCollector collector = new ComponentCollector();
            
            public Optional<FormattedText> accept(Style style, String string) {
                characterLimitingVisitor.resetLength();
                if (!StringDecomposer.iterateFormatted(string, style, characterLimitingVisitor)) {
                    String string2 = string.substring(0, characterLimitingVisitor.getLength());
                    if (!string2.isEmpty()) {
                        this.collector.append(FormattedText.of(string2, style));
                    }
                    
                    return Optional.of(this.collector.getResultOrEmpty());
                } else {
                    if (!string.isEmpty()) {
                        this.collector.append(FormattedText.of(string, style));
                    }
                    
                    return Optional.empty();
                }
            }
        }, Style.EMPTY).orElse(text);
        gui.drawString(matrices, text, x, y, colour);
    }
    
    static class CharacterLimitingVisitor implements FormattedCharSink {
        private int widthLeft;
        private int length;
        
        public CharacterLimitingVisitor(int maxWidth) {
            this.widthLeft = maxWidth;
        }
        
        @Override
        public boolean accept(int i, Style style, int j) {
            this.widthLeft--;
            if (this.widthLeft >= 0.0F) {
                this.length = i + Character.charCount(j);
                return true;
            } else {
                return false;
            }
        }
        
        public int getLength() {
            return this.length;
        }
        
        public void resetLength() {
            this.length = 0;
        }
    }
}
