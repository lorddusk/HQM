package hardcorequesting.common.quests.task;

import hardcorequesting.common.quests.Quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    
    protected abstract void onAddElement();
    
    protected abstract void onModifyElement();
    
    protected final List<T> getShownElements() {
        if (Quest.canQuestsBeEdited()) {
            return elementsWithEmpty;
        } else {
            return elements;
        }
    }
    
    protected final T getOrCreateForModify(int id) {
        if (id >= elements.size()) {
            T element = createEmpty();
            elements.add(element);
            onAddElement();
            return element;
        } else {
            onModifyElement();
            return elements.get(id);
        }
    }
    
    protected final void setElement(int id, T element) {
        Objects.requireNonNull(element);
        if (id >= elements.size()) {
            elements.add(element);
            onAddElement();
        } else {
            elements.set(id, element);
            onModifyElement();
        }
    }
}
