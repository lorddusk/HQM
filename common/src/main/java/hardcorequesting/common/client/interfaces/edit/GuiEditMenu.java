package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class GuiEditMenu extends Graphic {
    
    public static final int BOX_OFFSET = 30;
    protected final GuiQuestBook gui;
    
    protected GuiEditMenu(GuiQuestBook gui, boolean isControlOnFirstPage) {
        this.gui = gui;
        int xOffset = isControlOnFirstPage ? 0 : 145;
    
        addClickable(new LargeButton(gui, "hqm.edit.ok", xOffset + 40, 200) {
            @Override
            public void onClick() {
                save();
                close();
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.edit.cancel", xOffset + 100, 200) {
            @Override
            public void onClick() {
                close();
            }
        });
    }
    
    public void close() {
        gui.setEditMenu(null);
    }
    
    public abstract void save();
}
