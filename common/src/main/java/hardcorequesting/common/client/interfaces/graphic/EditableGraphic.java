package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
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
    
        gui.drawEditButtons(matrices, mX, mY, editButtons);
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.drawTooltip(matrices, gui, mX, mY);
    
        gui.drawEditButtonTooltip(matrices, mX, mY, editButtons);
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
    
        gui.handleEditButtonClick(mX, mY, editButtons);
    }
    
    @Override
    public boolean keyPressed(GuiQuestBook gui, int keyCode) {
        return KeyboardHandler.handleEditModeHotkey(keyCode, editButtons)
                || super.keyPressed(gui, keyCode);
    }
    
    protected void setEditMode(EditMode mode) {
        gui.setCurrentMode(mode);
    }
}
