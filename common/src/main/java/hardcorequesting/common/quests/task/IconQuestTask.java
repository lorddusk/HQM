package hardcorequesting.common.quests.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 * Provides the layout of a vertical list of elements, where each entry has an item icon, a name, and any extra info.
 */
public abstract class IconQuestTask<T extends IconQuestTask.IconTask> extends ListTask<T> {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    public IconQuestTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    protected abstract void onRemoveElement();
    
    @Environment(EnvType.CLIENT)
    protected abstract void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, T element, int index, int x, int y);
    
    @Environment(EnvType.CLIENT)
    protected abstract void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, T element);
    
    protected void setIcon(int id, ItemStack stack) {
        getOrCreateForModify(id).setIconStack(stack);
    }
    
    protected void setName(int id, String str) {
        getOrCreateForModify(id).setName(str);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public final void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<T> renderElements = getShownElements();
        for (int i = 0; i < renderElements.size(); i++) {
            T element = renderElements.get(i);
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(element.getIconStack(), x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(element.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            drawElementText(matrices, gui, player, element, i, x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public final void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<T> elements = getShownElements();
            for (int i = 0; i < elements.size(); i++) {
                T element = elements.get(i);
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    final int id = i;
                    switch (gui.getCurrentMode()) {
                        case ITEM:
                            PickItemMenu.display(gui, player, element.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> setIcon(id, result.get()));
                            break;
                        case RENAME:
                            GuiEditMenuTextEditor.display(gui, player, element.getName(), 110,
                                    result -> setName(id, result));
                            break;
                        case DELETE:
                            if (i < this.elements.size()) {
                                this.elements.remove(i);
                                onRemoveElement();
                            }
                            break;
                        default:
                            handleElementEditClick(gui, player, gui.getCurrentMode(), id, element);
                    }
                    
                    break;
                }
            }
        }
    }
    
    protected abstract static class IconTask {
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
