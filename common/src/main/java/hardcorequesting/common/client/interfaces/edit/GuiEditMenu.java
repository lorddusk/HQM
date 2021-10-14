package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class GuiEditMenu extends Graphic {
    
    public static final int BOX_OFFSET = 30;
    protected final List<AbstractCheckBox> checkboxes = new ArrayList<>();
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
    
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        for (AbstractCheckBox checkbox : checkboxes) {
            checkbox.draw(matrices, mX, mY);
        }
    }
    
    public void onClick(int mX, int mY, int b) {
        if (!hasButtons && b == 1) {
            save();
            close();
            return;
        }
        
        super.onClick(mX, mY, b);
        
        for (AbstractCheckBox checkbox : checkboxes) {
            checkbox.onClick(mX, mY);
        }
    }
    
    public void close() {
        gui.setEditMenu(null);
    }
    
    public abstract void save();
    
    public boolean hasButtons() {
        return hasButtons;
    }
}
