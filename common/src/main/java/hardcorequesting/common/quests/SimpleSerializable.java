package hardcorequesting.common.quests;

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
            parent.resolveData(filePath(), path -> {
                path.set(saveToString());
            });
        } else {
            parent.resolve(filePath(), path -> {
                path.set(saveToString());
            });
        }
    }
    
    @Override
    public final void load() {
        loadFromString(Optional.empty());
        Optional<QuestLine.FileProvider> provider;
        if (isData()) {
            provider = parent.resolveData(filePath());
        } else {
            provider = parent.resolve(filePath());
        }
        provider.map(QuestLine.FileProvider::get).ifPresent(this::loadFromString);
    }
}
