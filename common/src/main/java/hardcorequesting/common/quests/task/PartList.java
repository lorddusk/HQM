package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public final class PartList<Part> implements Iterable<Part> {
    private final Supplier<Part> emptySupplier;
    private final EditType.Type type;
    private final int limit;
    
    private final List<Part> elements;
    private final List<Part> elementsWithEmpty;
    
    public PartList(Supplier<Part> emptySupplier, EditType.Type type, int limit) {
        this.emptySupplier = emptySupplier;
        this.type = type;
        this.limit = limit;
    
        List<Part> list = new ArrayList<>();
        list.add(emptySupplier.get());
        elements = list.subList(0, 0);
        elementsWithEmpty = Collections.unmodifiableList(list);
    }
    
    public List<Part> getShownElements() {
        if (Quest.canQuestsBeEdited() && elements.size() < limit) {
            return elementsWithEmpty;
        } else {
            return elements;
        }
    }
    
    public Part getOrCreateForModify(int id) {
        if (id >= elements.size()) {
            Part element = emptySupplier.get();
            elements.add(element);
            SaveHelper.add(EditType.BaseEditType.ADD.with(type));
            return element;
        } else {
            SaveHelper.add(EditType.BaseEditType.CHANGE.with(type));
            return elements.get(id);
        }
    }
    
    public void set(int id, Part element) {
        Objects.requireNonNull(element);
        if (id >= elements.size()) {
            elements.add(element);
            SaveHelper.add(EditType.BaseEditType.ADD.with(type));
        } else {
            elements.set(id, element);
            SaveHelper.add(EditType.BaseEditType.CHANGE.with(type));
        }
    }
    
    public void remove(int id) {
        if (id < this.elements.size()) {
            this.elements.remove(id);
            SaveHelper.add(EditType.BaseEditType.REMOVE.with(type));
        }
    }
    
    public Part get(int id) {
        return elements.get(id);
    }
    
    public int size() {
        return elements.size();
    }
    
    public boolean isEmpty() {
        return elements.isEmpty();
    }
    
    @Deprecated
    public List<Part> getElements() {
        return elements;
    }
    
    public JsonArray write(TypeAdapter<Part> adapter) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (Part part : elements) {
            array.add(adapter.toJsonTree(part));
        }
        return array.build();
    }
    
    public void read(JsonArray array, TypeAdapter<Part> adapter) {
        elements.clear();
        for (JsonElement element : array) {
            Part part = adapter.fromJsonTree(element);
            if (part != null)
                elements.add(part);
        }
    }
    
    @NotNull
    @Override
    public Iterator<Part> iterator() {
        return elements.iterator();
    }
}
