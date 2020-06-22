package hardcorequesting.client;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.client.util.TextCollector;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class EditButton {
    
    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_ICON_SIZE = 12;
    private static final int BUTTON_ICON_SRC_X = 0;
    private static final int BUTTON_ICON_SRC_Y = 0;
    private static final int EDIT_BUTTONS_PER_ROW = 2;
    private static final int EDIT_BUTTONS_SRC_PER_ROW = 8;
    
    private GuiQuestBook guiQuestBook;
    private int x;
    private int y;
    private EditMode mode;
    private List<StringRenderable> text;
    
    public EditButton(GuiQuestBook guiQuestBook, EditMode mode, int id) {
        this.guiQuestBook = guiQuestBook;
        this.mode = mode;
        
        int x = id % EDIT_BUTTONS_PER_ROW;
        int y = id / EDIT_BUTTONS_PER_ROW;
        
        this.x = -38 + x * 20;
        this.y = 5 + y * 20;
    }
    
    public static EditButton[] createButtons(GuiQuestBook gui, EditMode... modes) {
        EditButton[] ret = new EditButton[modes.length];
        for (int i = 0; i < modes.length; i++) {
            EditMode mode = modes[i];
            ret[i] = new EditButton(gui, mode, i);
        }
        return ret;
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(int mX, int mY) {
        int srcY = guiQuestBook.getCurrentMode() == mode ? 2 : guiQuestBook.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY) ? 1 : 0;
        guiQuestBook.drawRect(x, y, 256 - BUTTON_SIZE, srcY * BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        guiQuestBook.drawRect(x + 2, y + 2,
                BUTTON_ICON_SRC_X + (mode.ordinal() % EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE,
                BUTTON_ICON_SRC_Y + (mode.ordinal() / EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE,
                BUTTON_ICON_SIZE, BUTTON_ICON_SIZE);
    }
    
    @Environment(EnvType.CLIENT)
    public void drawInfo(MatrixStack matrices, int mX, int mY) {
        if (guiQuestBook.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
            if (text == null) {
                text = guiQuestBook.getLinesFromText(Translator.plain(mode.getName() + "\n\n" + mode.getDescription()), 1F, 150);
                for (int i = 1; i < text.size(); i++) {
                    TextCollector collector = new TextCollector();
                    text.get(i).visit((style, string) -> {
                        collector.add(StringRenderable.styled(string, style));
                        return Optional.empty();
                    }, Style.EMPTY);
                    text.set(i, collector.getCombined());
                }
            }
            
            guiQuestBook.renderTooltip(matrices, text, mX + guiQuestBook.getLeft(), mY + guiQuestBook.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    public boolean onClick(int mX, int mY) {
        if (guiQuestBook.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
            guiQuestBook.setCurrentMode(mode);
            guiQuestBook.modifyingQuest = null;
            guiQuestBook.modifyingBar = null;
            return true;
        }
        
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public boolean click() {
        return onClick(x, y);
    }
    
    public boolean matchesMode(EditMode mode) {
        return this.mode == mode;
    }
}
