package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.icon.IconLayoutTask;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class IconTaskGraphic<Part extends IconLayoutTask.Part> implements TaskGraphic {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    private final IconLayoutTask<Part, ?> task;
    
    public IconTaskGraphic(IconLayoutTask<Part, ?> task) {
        this.task = task;
    }
    
    @Environment(EnvType.CLIENT)
    protected abstract void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Part part, int id, int x, int y);
    
    @Environment(EnvType.CLIENT)
    protected abstract void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Part part);
    
    protected List<Positioned<Part>> positionParts(List<Part> parts) {
        List<Positioned<Part>> list = new ArrayList<>(parts.size());
        int x = START_X;
        int y = START_Y;
        for (Part part : parts) {
            list.add(new Positioned<>(x, y, part));
            y += Y_OFFSET;
        }
        return list;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Positioned<Part>> renderElements = positionParts(task.parts.getShownElements());
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<Part> pos = renderElements.get(i);
            Part part = pos.getElement();
            int textX = pos.getX() + X_TEXT_OFFSET, textY = pos.getY() + Y_TEXT_OFFSET;
        
            gui.drawItemStack(part.getIconStack(), pos.getX(), pos.getY(), mX, mY, false);
            gui.drawString(matrices, Translator.plain(part.getName()), textX, textY, 0x404040);
            drawElementText(matrices, gui, player, part, i, textX + X_TEXT_INDENT, textY + 9);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Positioned<Part>> elements = positionParts(task.parts.getShownElements());
            for (int i = 0; i < elements.size(); i++) {
                Positioned<Part> pos = elements.get(i);
                Part part = pos.getElement();
            
                if (gui.inBounds(pos.getX(), pos.getY(), ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    final int id = i;
                    switch (gui.getCurrentMode()) {
                        case ITEM:
                            PickItemMenu.display(gui, player, part.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> task.setIcon(id, result.get()));
                            break;
                        case RENAME:
                            TextMenu.display(gui, player, part.getName(), 110,
                                    result -> task.setName(id, result));
                            break;
                        case DELETE:
                            task.parts.remove(i);
                            break;
                        default:
                            handleElementEditClick(gui, player, gui.getCurrentMode(), id, part);
                    }
                
                    break;
                }
            }
        }
    }
}
