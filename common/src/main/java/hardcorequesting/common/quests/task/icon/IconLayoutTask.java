package hardcorequesting.common.quests.task.icon;

import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.util.EditType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 * Provides the layout of a vertical list of elements, where each entry has an item icon, a name, and any extra info.
 */
public abstract class IconLayoutTask<T extends IconLayoutTask.Part, Data extends TaskData> extends QuestTask<Data> {
    
    protected static final int LIMIT = 4;
    
    public final PartList<T> parts;
    
    public IconLayoutTask(Class<Data> dataType, EditType.Type type, Quest parent, String description, String longDescription) {
        super(dataType, parent, description, longDescription);
        parts = new PartList<>(this::createEmpty, type, LIMIT);
    }
    
    
    protected abstract T createEmpty();
    
    public void setIcon(int id, ItemStack stack) {
        parts.getOrCreateForModify(id).setIconStack(stack);
    }
    
    public void setName(int id, String str) {
        parts.getOrCreateForModify(id).setName(str);
    }
    
    public abstract static class Part {
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
