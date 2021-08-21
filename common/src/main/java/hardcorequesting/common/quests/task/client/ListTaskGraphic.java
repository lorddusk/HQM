package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.util.Positioned;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class ListTaskGraphic<Part> implements TaskGraphic {
    
    private final PartList<Part> parts;
    
    public ListTaskGraphic(PartList<Part> parts) {
        this.parts = parts;
    }
    
    protected abstract List<Positioned<Part>> positionParts(List<Part> parts);
    
    protected abstract void drawPart(PoseStack matrices, GuiQuestBook gui, Player player, Part part, int id, int x, int y, int mX, int mY);
    
    protected abstract boolean isInPartBounds(GuiQuestBook gui, int mX, int mY, Positioned<Part> pos);
    
    protected boolean handlePartClick(GuiQuestBook gui, Player player, EditMode mode, Part part, int id) {
        if (mode == EditMode.DELETE) {
            parts.remove(id);
            return true;
        }
        return false;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Positioned<Part>> renderElements = positionParts(parts.getShownElements());
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<Part> pos = renderElements.get(i);
            Part part = pos.getElement();
            drawPart(matrices, gui, player, part, i, pos.getX(), pos.getY(), mX, mY);
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Positioned<Part>> elements = positionParts(parts.getShownElements());
            for (int i = 0; i < elements.size(); i++) {
                Positioned<Part> pos = elements.get(i);
                Part part = pos.getElement();
                
                if (isInPartBounds(gui, mX, mY, pos)) {
                    handlePartClick(gui, player, gui.getCurrentMode(), part, i);
                    break;
                }
            }
        }
    }
    
}
