package hardcorequesting.common.quests.task.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.task.ListTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 * Provides the layout of a vertical list of elements, where each entry has an item icon, a name, and any extra info.
 */
public abstract class IconLayoutTask<T extends IconLayoutTask.Part, Data extends QuestDataTask> extends ListTask<T, Data> {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    public IconLayoutTask(EditType.Type type, Quest parent, String description, String longDescription) {
        super(type, parent, description, longDescription);
    }
    
    @Environment(EnvType.CLIENT)
    protected abstract void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, T part, int index, int x, int y);
    
    @Environment(EnvType.CLIENT)
    protected abstract void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, T part);
    
    protected void setIcon(int id, ItemStack stack) {
        getOrCreateForModify(id).setIconStack(stack);
    }
    
    protected void setName(int id, String str) {
        getOrCreateForModify(id).setName(str);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Positioned<T>> renderElements = positionParts(getShownElements());
        for (int i = 0; i < renderElements.size(); i++) {
            Positioned<T> pos = renderElements.get(i);
            T part = pos.getElement();
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
            List<Positioned<T>> elements = positionParts(getShownElements());
            for (int i = 0; i < elements.size(); i++) {
                Positioned<T> pos = elements.get(i);
                T part = pos.getElement();
                
                if (gui.inBounds(pos.getX(), pos.getY(), ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    final int id = i;
                    switch (gui.getCurrentMode()) {
                        case ITEM:
                            PickItemMenu.display(gui, player, part.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> setIcon(id, result.get()));
                            break;
                        case RENAME:
                            GuiEditMenuTextEditor.display(gui, player, part.getName(), 110,
                                    result -> setName(id, result));
                            break;
                        case DELETE:
                            if (i < this.elements.size()) {
                                this.elements.remove(i);
                                SaveHelper.add(EditType.BaseEditType.REMOVE.with(type));
                            }
                            break;
                        default:
                            handleElementEditClick(gui, player, gui.getCurrentMode(), id, part);
                    }
                    
                    break;
                }
            }
        }
    }
    
    protected List<Positioned<T>> positionParts(List<T> parts) {
        List<Positioned<T>> list = new ArrayList<>(parts.size());
        int x = START_X;
        int y = START_Y;
        for (T part : parts) {
            list.add(new Positioned<>(x, y, part));
            y += Y_OFFSET;
        }
        return list;
    }
    
    protected abstract static class Part {
        private ItemStack iconStack = ItemStack.EMPTY;
        private String name = "New";
        
        public ItemStack getIconStack() {
            return iconStack;
        }
    
        public void setIconStack(@NotNull ItemStack iconStack) {
            this.iconStack = iconStack;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    }
}
