package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EditableGraphic extends Graphic {
    protected final GuiQuestBook gui;
    private final EditButton[] editButtons;
    
    public EditableGraphic(GuiQuestBook gui, EditMode... modes) {
        this.gui = gui;
        editButtons = EditButton.createButtons(this::setEditMode, modes);
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
    
        if (Quest.canQuestsBeEdited()) {
            for (EditButton button : editButtons) {
                button.draw(gui, matrices, mX, mY);
            }
        }
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.drawTooltip(matrices, gui, mX, mY);
    
        if (Quest.canQuestsBeEdited()) {
            for (EditButton button : editButtons) {
                button.drawInfo(gui, matrices, mX, mY);
            }
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int button) {
        super.onClick(gui, mX, mY, button);
    
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
        return KeyboardHandler.handleEditModeHotkey(keyCode, editButtons)
                || super.keyPressed(keyCode);
    }
    
    protected void setEditMode(EditMode mode) {
        gui.setCurrentMode(mode);
    }
}
