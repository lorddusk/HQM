package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.quests.Quest;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;

public class GuiEditMenuCommandEditor extends GuiEditMenuTextEditor {

    private Quest quest;
    private int id;
    private String[] commands;
    private boolean[] edited;
    private String added;

    public GuiEditMenuCommandEditor(GuiQuestBook gui, EntityPlayer player) {
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
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (this.commands[i].isEmpty()) {
                    drawStringTrimmed(gui, Translator.translate("hqm.commandEdit.deleted"), 190, 65 + (i * 10), 0xFF0000);
                } else {
                    drawStringTrimmed(gui, this.commands[i], 190, 65 + (i * 10), edited[i] ? 0xFF4500 : 0x000000);
                }
            }
        }
        if (this.added != null && !this.added.isEmpty()) {
            drawStringTrimmed(gui, this.added, 190, 65 + (i * 10), 0x447449);
        }
    }

    @Override
    public void drawMouseOver(GuiBase gui, int mX, int mY) {
        super.drawMouseOver(gui, mX, mY);
        int i = 0;
        if (this.commands != null && this.commands.length > 0) {
            for (; i < this.commands.length; i++) {
                if (mX > 190 && mX < 300 && mY > 65 + (i * 10) && mY < 65 + ((i + 1) * 10)) {
                    if (this.commands[i].isEmpty()) {
                        drawStringTrimmed(gui, Translator.translate("hqm.commandEdit.deleted"), 190, 65 + (i * 10), 0xF76767);
                    } else {
                        drawStringTrimmed(gui, this.commands[i], 190, 65 + (i * 10), edited[i] ? 0xF9AB7A : 0x969696);
                    }
                }
            }
        }
        if (this.added != null && !this.added.isEmpty()) {
            drawStringTrimmed(gui, this.added, 190, 65 + (i * 10), 0x5A9B60);
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

    private void drawStringTrimmed(GuiBase gui, String s, int x, int y, int colour) {
        int maxLength = Math.min(25, s.length());
        gui.drawString(s.substring(0, maxLength) + (maxLength < s.length() ? "..." : ""), x, y, colour);
    }
}
