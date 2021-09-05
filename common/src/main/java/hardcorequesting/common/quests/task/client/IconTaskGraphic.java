package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.icon.IconLayoutTask;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class IconTaskGraphic<Part extends IconLayoutTask.Part> extends ListTaskGraphic<Part> {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    private final IconLayoutTask<Part, ?> task;
    
    public IconTaskGraphic(IconLayoutTask<Part, ?> task, PartList<Part> parts) {
        super(parts);
        this.task = task;
    }
    
    protected abstract void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Part part, int id, int x, int y);
    
    @Override
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
    
    @Override
    protected void drawPart(PoseStack matrices, GuiQuestBook gui, Player player, Part part, int id, int x, int y, int mX, int mY) {
        int textX = x + X_TEXT_OFFSET, textY = y + Y_TEXT_OFFSET;
        part.getIconStack().ifLeft(itemStack -> gui.drawItemStack(matrices, itemStack, x, y, mX, mY, false))
                .ifRight(fluidStack -> gui.drawFluid(fluidStack, matrices, x, y, mX, mY));
        
        gui.drawString(matrices, Translator.plain(part.getName()), textX, textY, 0x404040);
        drawElementText(matrices, gui, player, part, id, textX + X_TEXT_INDENT, textY + 9);
    }
    
    @Override
    protected boolean handlePartClick(GuiQuestBook gui, Player player, EditMode mode, Part part, int id) {
        if (mode == EditMode.ITEM) {
            PickItemMenu.display(gui, player, part.getIconStack(), PickItemMenu.Type.ITEM_FLUID,
                    result -> task.setIcon(id, result.get()));
            return true;
        } else if (mode == EditMode.RENAME) {
            TextMenu.display(gui, player, part.getName(), 110,
                    result -> task.setName(id, result));
            return true;
        } else {
            return super.handlePartClick(gui, player, mode, part, id);
        }
    }
    
    @Override
    protected boolean isInPartBounds(GuiQuestBook gui, int mX, int mY, Positioned<Part> pos) {
        return gui.inBounds(pos.getX(), pos.getY(), ITEM_SIZE, ITEM_SIZE, mX, mY);
    }
}
