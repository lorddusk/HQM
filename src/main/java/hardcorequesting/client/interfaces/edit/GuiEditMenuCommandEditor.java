package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.quests.Quest;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.client.util.TextCollector;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Style;

import java.util.Optional;

public class GuiEditMenuCommandEditor extends GuiEditMenuTextEditor {
    
    private Quest quest;
    private int id;
    private String[] commands;
    private boolean[] edited;
    private String added;
    
    public GuiEditMenuCommandEditor(GuiQuestBook gui, PlayerEntity player) {
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
    public void draw(MatrixStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (this.commands[i].isEmpty()) {
                    drawStringTrimmed(matrices, gui, Translator.translated("hqm.commandEdit.deleted"), 190, 65 + (i * 10), 0xFF0000);
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
    public void renderTooltip(MatrixStack matrices, GuiBase gui, int mX, int mY) {
        super.renderTooltip(matrices, gui, mX, mY);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (mX > 190 && mX < 300 && mY > 65 + (i * 10) && mY < 65 + ((i + 1) * 10)) {
                    if (this.commands[i].isEmpty()) {
                        drawStringTrimmed(matrices, gui, Translator.translated("hqm.commandEdit.deleted"), 190, 65 + (i * 10), 0xF76767);
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
    
    private void drawStringTrimmed(MatrixStack matrices, GuiBase gui, StringRenderable text, int x, int y, int colour) {
        CharacterLimitingVisitor characterLimitingVisitor = new CharacterLimitingVisitor(25);
        text = text.visit(new StringRenderable.StyledVisitor<StringRenderable>() {
            private final TextCollector collector = new TextCollector();
            
            public Optional<StringRenderable> accept(Style style, String string) {
                characterLimitingVisitor.resetLength();
                if (!TextVisitFactory.visitFormatted(string, style, characterLimitingVisitor)) {
                    String string2 = string.substring(0, characterLimitingVisitor.getLength());
                    if (!string2.isEmpty()) {
                        this.collector.add(StringRenderable.styled(string2, style));
                    }
                    
                    return Optional.of(this.collector.getCombined());
                } else {
                    if (!string.isEmpty()) {
                        this.collector.add(StringRenderable.styled(string, style));
                    }
                    
                    return Optional.empty();
                }
            }
        }, Style.EMPTY).orElse(text);
        gui.drawString(matrices, text, x, y, colour);
    }
    
    static class CharacterLimitingVisitor implements TextVisitFactory.CharacterVisitor {
        private int widthLeft;
        private int length;
        
        public CharacterLimitingVisitor(int maxWidth) {
            this.widthLeft = maxWidth;
        }
        
        public boolean onChar(int i, Style style, int j) {
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
