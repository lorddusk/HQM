package hardcorequesting.common.quests.task.icon;

import com.mojang.datafixers.util.Either;
import hardcorequesting.common.platform.FluidStack;
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
    
    protected final PartList<T> parts;
    
    public IconLayoutTask(Class<Data> dataType, EditType.Type type, Quest parent, String description, String longDescription) {
        super(dataType, parent, description, longDescription);
        parts = new PartList<>(this::createEmpty, type, LIMIT);
    }
    
    
    protected abstract T createEmpty();
    
    public void setIcon(int id, Either<ItemStack, FluidStack> stack) {
        parts.getOrCreateForModify(id).setIconStack(stack);
    }
    
    public void setName(int id, String str) {
        parts.getOrCreateForModify(id).setName(str);
    }
    
    public abstract static class Part {
        private Either<ItemStack, FluidStack> iconStack = Either.left(ItemStack.EMPTY);
        private String name = "New";
        
        public Either<ItemStack, FluidStack> getIconStack() {
            return iconStack;
        }
        
        public boolean hasNoIcon() {
            return iconStack.map(ItemStack::isEmpty, FluidStack::isEmpty);
        }
        
        public void setIconStack(@NotNull Either<ItemStack, FluidStack> iconStack) {
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
