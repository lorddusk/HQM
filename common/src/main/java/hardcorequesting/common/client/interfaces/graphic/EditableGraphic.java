package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A graphic which has a set of edit mode buttons to the left of the gui when editing quest line data.
 * Is intended to be used specifically by graphics associated with a {@link hardcorequesting.common.client.BookPage}.
 */
@Environment(EnvType.CLIENT)
public abstract class EditableGraphic extends Graphic {
    protected final GuiQuestBook gui;
    private final EditButton[] editButtons;
    
    public EditableGraphic(GuiQuestBook gui, EditMode... modes) {
        this.gui = gui;
        editButtons = EditButton.createButtons(this::setEditMode, modes);
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        super.draw(graphics, mX, mY);
    
        if (Quest.canQuestsBeEdited()) {
            for (EditButton button : editButtons) {
                button.draw(gui, graphics, mX, mY);
            }
        }
    }
    
    @Override
    public void drawTooltip(GuiGraphics graphics, int mX, int mY) {
        super.drawTooltip(graphics, mX, mY);
    
        if (Quest.canQuestsBeEdited()) {
            for (EditButton button : editButtons) {
                button.drawInfo(gui, graphics, mX, mY);
            }
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int button) {
        super.onClick(mX, mY, button);
    
        if (Quest.canQuestsBeEdited()) {
            for (EditButton editButton : editButtons) {
                if (editButton.onClick(gui, mX, mY)) {
                    break;
                }
            }
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        return super.keyPressed(keyCode)
                || KeyboardHandler.handleEditModeHotkey(keyCode, editButtons);
    }
    
    protected void setEditMode(EditMode mode) {
        gui.setCurrentMode(mode);
    }
}
