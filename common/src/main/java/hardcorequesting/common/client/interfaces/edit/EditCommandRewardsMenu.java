package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.ComponentCollector;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditCommandRewardsMenu extends AbstractTextMenu {
    
    private static final int START_X = 190, START_Y = 65;
    private static final int END_X = 300;
    private static final int LINE_HEIGHT = 10;
    
    private final Quest quest;
    private int id;
    private final List<Entry> commands;
    private String added;
    
    private record Entry(String command, boolean edited)
    {}
    
    public static void display(GuiQuestBook gui, Quest quest) {
        gui.setEditMenu(new EditCommandRewardsMenu(gui, quest));
    }
    
    private EditCommandRewardsMenu(GuiQuestBook gui, Quest quest) {
        super(gui, "");
        this.quest = quest;
        this.commands = this.quest.getRewards().getCommandRewardsAsStrings().stream()
                .map(s -> new Entry(s, false)).collect(Collectors.toCollection(ArrayList::new));
        
        this.id = -1;
        if (gui.getCurrentMode() == EditMode.COMMAND_CHANGE) {
            if (!this.commands.isEmpty()) {
                this.id = this.commands.size() - 1;
                this.textLogic.setTextAndCursor(this.commands.get(this.id).command);
            }
        }
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        for (int i = 0; i < this.commands.size(); i++) {
            Entry entry = commands.get(i);
            if (entry.command.isEmpty()) {
                drawStringTrimmed(matrices, gui, Translator.translatable("hqm.commandEdit.deleted"), START_X, START_Y + (i * LINE_HEIGHT), 0xFF0000);
            } else {
                drawStringTrimmed(matrices, gui, Translator.plain(entry.command), START_X, getLineY(i), entry.edited ? 0xFF4500 : 0x000000);
            }
        }
        if (this.added != null && !this.added.isEmpty()) {
            drawStringTrimmed(matrices, gui, Translator.plain(this.added), START_X, getLineY(getLineForAdded()), 0x447449);
        }
    }
    
    @Override
    public void save() {
        storeCurrentText();
    
        if (this.added != null && !this.added.isEmpty()) {
            quest.getRewards().addCommand(this.added);
            SaveHelper.add(EditType.COMMAND_ADD);
        }
        for (int i = this.commands.size() - 1; i >= 0; i--) {
            Entry entry = commands.get(i);
            if (entry.edited) {
                if (entry.command.isEmpty()) {
                    quest.getRewards().removeCommand(i);
                    SaveHelper.add(EditType.COMMAND_REMOVE);
                } else {
                    quest.getRewards().editCommand(i, entry.command);
                    SaveHelper.add(EditType.COMMAND_CHANGE);
                }
            }
        }
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        super.drawTooltip(matrices, mX, mY);
        
        for (int i = 0; i < this.commands.size(); i++) {
            if (isOnCommand(i, mX, mY)) {
                Entry entry = this.commands.get(i);
                if (entry.command.isEmpty()) {
                    drawStringTrimmed(matrices, gui, Translator.translatable("hqm.commandEdit.deleted"), START_X, getLineY(i), 0xF76767);
                } else {
                    drawStringTrimmed(matrices, gui, Translator.plain(entry.command), START_X, getLineY(i), entry.edited ? 0xF9AB7A : 0x969696);
                }
            }
        }
        if (this.added != null && !this.added.isEmpty()) {
            drawStringTrimmed(matrices, gui, Translator.plain(this.added), START_X, getLineY(getLineForAdded()), 0x5A9B60);
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        for (int i = 0; i < this.commands.size(); i++) {
            if (isOnCommand(i, mX, mY)) {
                selectCommand(i);
                return;
            }
        }
        
        if (isOnCommand(getLineForAdded(), mX, mY)) {
            selectAddedCommand();
        }
    }
    
    private boolean isOnCommand(int id, int mX, int mY) {
        int minY = getLineY(id);
        return START_X < mX && mX < END_X && minY < mY && mY < minY + LINE_HEIGHT;
    }
    
    private int getLineY(int line) {
        return START_Y + (line * LINE_HEIGHT);
    }
    
    private int getLineForAdded() {
        return commands.size();
    }
    
    private void selectCommand(int id) {
        if (this.id == id) return;
        storeCurrentText();
        this.id = id;
        this.textLogic.setTextAndCursor(this.commands.get(id).command);
    }
    
    private void selectAddedCommand() {
        if (this.id == -1) return;
        storeCurrentText();
        this.id = -1;
        this.textLogic.setTextAndCursor(this.added);
    }
    
    private void storeCurrentText() {
        String command = this.textLogic.getText();
        if (this.id < 0) this.added = command;
        else {
            if (!this.commands.get(this.id).command.equals(command)) {
                this.commands.set(this.id, new Entry(command, true));
            }
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
