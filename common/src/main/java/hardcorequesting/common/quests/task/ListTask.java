package hardcorequesting.common.quests.task;

import hardcorequesting.common.quests.Quest;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A base class for tasks with sub-elements.
 */
public abstract class ListTask<T> extends QuestTask {
    
    public final List<T> elements;
    private final List<T> elementsWithEmpty;
    {
        List<T> list = new ArrayList<>();
        list.add(createEmpty());
        elements = list.subList(0, 0);
        elementsWithEmpty = Collections.unmodifiableList(list);
    }
    
    public ListTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    protected abstract T createEmpty();
    
    protected abstract void onAddElement(Player player);
    
    protected abstract void onModifyElement();
    
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
            onModifyElement();
            return elements.get(id);
        }
    }
}
