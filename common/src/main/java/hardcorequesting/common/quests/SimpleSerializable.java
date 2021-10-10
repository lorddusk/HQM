package hardcorequesting.common.quests;

import hardcorequesting.common.io.FileProvider;

import java.util.Optional;

public abstract class SimpleSerializable implements StringSerializable, Serializable {
    protected final QuestLine parent;
    
    public SimpleSerializable(QuestLine parent) {
        this.parent = parent;
    }
    
    public abstract String filePath();
    
    @Override
    public final void save() {
        if (isData()) {
            parent.resolveData(filePath()).set(saveToString());
        } else {
            parent.resolve(filePath()).set(saveToString());
        }
    }
    
    @Override
    public final void load() {
        loadFromString(Optional.empty());
        FileProvider provider;
        if (isData()) {
            provider = parent.resolveData(filePath());
        } else {
            provider = parent.resolve(filePath());
        }
        loadFromString(provider.get());
    }
}
