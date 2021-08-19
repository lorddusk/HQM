package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A base class for tasks with sub-elements.
 */
public abstract class ListTask<T, Data extends QuestDataTask> extends QuestTask<Data> {
    
    protected final EditType.Type type;
    public final List<T> elements;
    private final List<T> elementsWithEmpty;
    {
        List<T> list = new ArrayList<>();
        list.add(createEmpty());
        elements = list.subList(0, 0);
        elementsWithEmpty = Collections.unmodifiableList(list);
    }
    
    public ListTask(EditType.Type type, Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        this.type = type;
    }
    
    protected abstract T createEmpty();
    
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
            SaveHelper.add(EditType.BaseEditType.ADD.with(type));
            return element;
        } else {
            SaveHelper.add(EditType.BaseEditType.CHANGE.with(type));
            return elements.get(id);
        }
    }
    
    protected final void setElement(int id, T element) {
        Objects.requireNonNull(element);
        if (id >= elements.size()) {
            elements.add(element);
            SaveHelper.add(EditType.BaseEditType.ADD.with(type));
        } else {
            elements.set(id, element);
            SaveHelper.add(EditType.BaseEditType.CHANGE.with(type));
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
