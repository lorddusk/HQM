package hardcorequesting.common.quests.task;

import hardcorequesting.common.quests.Quest;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 */
public abstract class IconQuestTask<T extends IconQuestTask.IconTask> extends QuestTask {
    
    public final List<T> elements;
    private final List<T> elementsWithEmpty;
    
    public IconQuestTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        List<T> list = new ArrayList<>();
        list.add(createEmpty());
        elements = list.subList(0, 0);
        elementsWithEmpty = Collections.unmodifiableList(list);
    }
    
    protected abstract T createEmpty();
    
    protected abstract void onAddElement(Player player);
    
    protected abstract void onModifyElement(Player player);
    
    protected final List<T> getShownElements() {
        if (Quest.canQuestsBeEdited()) {
            return elementsWithEmpty;
        } else {
            return elements;
        }
    }
    
    protected final T getOrCreateForModify(int id, Player player) {
        if (id >= elements.size()) {
            T element = createEmpty();
            elements.add(element);
            onAddElement(player);
            return element;
        } else {
            onModifyElement(player);
            return elements.get(id);
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
