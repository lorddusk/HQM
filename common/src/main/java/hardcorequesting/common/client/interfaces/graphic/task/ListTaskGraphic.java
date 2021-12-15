package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.util.Positioned;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class ListTaskGraphic<Part> extends TaskGraphic {
    
    protected final PartList<Part> parts;
    
    public ListTaskGraphic(QuestTask<?> task, PartList<Part> parts, UUID playerId, GuiQuestBook gui) {
        super(playerId, gui, task);
        this.parts = parts;
    }
    
    protected abstract List<Positioned<Part>> positionParts(List<Part> parts);
    
    protected abstract void drawPart(PoseStack matrices, Part part, int id, int x, int y, int mX, int mY);
    
    protected List<FormattedText> getPartTooltip(Positioned<Part> pos, int id, int mX, int mY) {return null;}
    
    protected abstract boolean isInPartBounds(int mX, int mY, Positioned<Part> pos);
    
    protected void handlePartClick(Part part, int id) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            handleEditPartClick(gui.getCurrentMode(), part, id);
        }
    }
    
    protected boolean handleEditPartClick(EditMode mode, Part part, int id) {
        if (mode == EditMode.DELETE) {
            parts.remove(id);
            return true;
        }
        return false;
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        List<Positioned<Part>> renderElements = positionParts(parts.getShownElements());
        
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<Part> pos = renderElements.get(i);
            Part part = pos.getElement();
            drawPart(matrices, part, i, pos.getX(), pos.getY(), mX, mY);
        }
        
        super.draw(matrices, mX, mY);
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        List<Positioned<Part>> renderElements = positionParts(parts.getShownElements());
    
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<Part> pos = renderElements.get(i);
            List<FormattedText> tooltip = getPartTooltip(pos, i, mX, mY);
            if (tooltip != null) {
                gui.renderTooltipL(matrices, tooltip, gui.getLeft() + mX, gui.getTop() + mY);
                return;
            }
        }
        
        super.drawTooltip(matrices, mX, mY);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        int id = getClickedPart(mX, mY);
        if (id >= 0)
            handlePartClick(parts.getShownElements().get(id), id);
        
        super.onClick(mX, mY, b);
    }
    
    protected final int getClickedPart(int mX, int mY) {
        List<Positioned<Part>> elements = positionParts(parts.getShownElements());
        for (int i = 0; i < elements.size(); i++) {
            if (isInPartBounds(mX, mY, elements.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
