package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class GuiEditMenu extends Graphic {
    
    public static final int BOX_OFFSET = 30;
    private boolean hasButtons;
    protected final GuiQuestBook gui;
    protected final UUID playerId;
    
    protected GuiEditMenu(GuiQuestBook gui, UUID playerId) {
        this.gui = gui;
        this.playerId = playerId;
    }
    
    protected GuiEditMenu(GuiQuestBook gui, UUID playerId, boolean isControlOnFirstPage) {
        this(gui, playerId);
        hasButtons = true;
        int xOffset = isControlOnFirstPage ? 0 : 145;
        
        addButton(new LargeButton(gui, "hqm.edit.ok", xOffset + 40, 200) {
            @Override
            public void onClick() {
                save();
                close();
            }
        });
        
        addButton(new LargeButton(gui, "hqm.edit.cancel", xOffset + 100, 200) {
            @Override
            public void onClick() {
                close();
            }
        });
    }
    
    public void onClick(int mX, int mY, int b) {
        if (!hasButtons && b == 1) {
            save();
            close();
            return;
        }
        
        super.onClick(mX, mY, b);
    }
    
    public void close() {
        gui.setEditMenu(null);
    }
    
    public abstract void save();
    
    public boolean hasButtons() {
        return hasButtons;
    }
}
