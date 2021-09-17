package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.util.Positioned;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class ListTaskGraphic<Part> extends TaskGraphic {
    
    protected final PartList<Part> parts;
    
    public ListTaskGraphic(PartList<Part> parts, UUID playerId) {
        super(playerId);
        this.parts = parts;
    }
    
    protected abstract List<Positioned<Part>> positionParts(List<Part> parts);
    
    protected abstract void drawPart(PoseStack matrices, GuiQuestBook gui, Part part, int id, int x, int y, int mX, int mY);
    
    protected List<FormattedText> getPartTooltip(GuiQuestBook gui, Positioned<Part> pos, int id, int mX, int mY) {return null;}
    
    protected abstract boolean isInPartBounds(GuiQuestBook gui, int mX, int mY, Positioned<Part> pos);
    
    protected boolean handlePartClick(GuiQuestBook gui, EditMode mode, Part part, int id) {
        if (mode == EditMode.DELETE) {
            parts.remove(id);
            return true;
        }
        return false;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        List<Positioned<Part>> renderElements = positionParts(parts.getShownElements());
        
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<Part> pos = renderElements.get(i);
            Part part = pos.getElement();
            drawPart(matrices, gui, part, i, pos.getX(), pos.getY(), mX, mY);
        }
        
        super.draw(matrices, gui, mX, mY);
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        List<Positioned<Part>> renderElements = positionParts(parts.getShownElements());
    
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<Part> pos = renderElements.get(i);
            List<FormattedText> tooltip = getPartTooltip(gui, pos, i, mX, mY);
            if (tooltip != null) {
                gui.renderTooltipL(matrices, tooltip, gui.getLeft() + mX, gui.getTop() + mY);
                return;
            }
        }
        
        super.drawTooltip(matrices, gui, mX, mY);
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            int id = getClickedPart(gui, mX, mY);
            if (id >= 0)
                handlePartClick(gui, gui.getCurrentMode(), parts.getShownElements().get(id), id);
        }
        
        super.onClick(gui, mX, mY, b);
    }
    
    protected final int getClickedPart(GuiQuestBook gui, int mX, int mY) {
        List<Positioned<Part>> elements = positionParts(parts.getShownElements());
        for (int i = 0; i < elements.size(); i++) {
            if (isInPartBounds(gui, mX, mY, elements.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
