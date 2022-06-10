package hardcorequesting.common.quests.task.icon;

import com.mojang.datafixers.util.Either;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.WrappedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 * Provides the layout of a vertical list of elements, where each entry has an item icon, a name, and any extra info.
 */
public abstract class IconLayoutTask<T extends IconLayoutTask.Part, Data extends TaskData> extends QuestTask<Data> {
    
    protected static final int LIMIT = 4;
    
    protected final PartList<T> parts;
    
    public IconLayoutTask(TaskType<? extends IconLayoutTask<T, Data>> type, Class<Data> dataType, EditType.Type editType,
                          Quest parent) {
        super(type, dataType, parent);
        parts = new PartList<>(this::createEmpty, editType, LIMIT);
    }
    
    public PartList<T> getParts() {
        return parts;
    }
    
    protected abstract T createEmpty();
    
    public void setIcon(int id, Either<ItemStack, FluidStack> stack) {
        parts.getOrCreateForModify(id).setIconStack(stack);
        parent.setIconIfEmpty(stack);
    }
    
    public void setName(int id, WrappedText str) {
        parts.getOrCreateForModify(id).setName(str);
    }
    
    public abstract static class Part {
        private Either<ItemStack, FluidStack> iconStack = Either.left(ItemStack.EMPTY);
        private WrappedText name = WrappedText.create("New");
        
        public Either<ItemStack, FluidStack> getIconStack() {
            return iconStack;
        }
        
        public boolean hasNoIcon() {
            return iconStack.map(ItemStack::isEmpty, FluidStack::isEmpty);
        }
        
        public void setIconStack(@NotNull Either<ItemStack, FluidStack> iconStack) {
            this.iconStack = iconStack;
        }
        
        public MutableComponent getName() {
            return name.getText();
        }
        
        public WrappedText getRawName() {
            return name;
        }
    
        public void setName(WrappedText name) {
            this.name = Objects.requireNonNull(name);
        }
    }
}
