package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import hardcorequesting.common.io.adapter.Adapter;
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
    
    protected final JsonArray writeElements(TypeAdapter<T> adapter) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (T part : elements) {
            array.add(adapter.toJsonTree(part));
        }
        return array.build();
    }
    
    protected final void readElements(JsonArray array, TypeAdapter<T> adapter) {
        elements.clear();
        for (JsonElement element : array) {
            T part = adapter.fromJsonTree(element);
            if (part != null)
                elements.add(part);
        }
    }
}
