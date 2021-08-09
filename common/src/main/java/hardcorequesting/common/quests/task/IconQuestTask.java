package hardcorequesting.common.quests.task;

import hardcorequesting.common.quests.Quest;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 */
public abstract class IconQuestTask<T extends IconQuestTask.IconTask> extends QuestTask {
    
    public final List<T> elements = new ArrayList<>();
    
    public IconQuestTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
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
        
        protected void copyFrom(IconTask other) {
            setIconStack(other.getIconStack().copy());
            setName(other.getName());
        }
    }
}
