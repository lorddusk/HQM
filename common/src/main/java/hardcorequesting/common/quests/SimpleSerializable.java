package hardcorequesting.common.quests;

import hardcorequesting.common.io.DataManager;
import hardcorequesting.common.io.FileProvider;

import java.util.Optional;

public abstract class SimpleSerializable implements StringSerializable, Serializable {
    protected final QuestLine parent;
    
    public SimpleSerializable(QuestLine parent) {
        this.parent = parent;
    }
    
    public abstract String filePath();
    
    @Override
    public final void save(DataManager dataManager) {
        if (isData()) {
            dataManager.resolveData(filePath()).set(saveToString());
        } else {
            dataManager.resolve(filePath()).set(saveToString());
        }
    }
    
    @Override
    public final void load(DataManager dataManager) {
        loadFromString(Optional.empty());
        FileProvider provider;
        if (isData()) {
            provider = dataManager.resolveData(filePath());
        } else {
            provider = dataManager.resolve(filePath());
        }
        loadFromString(provider.get());
    }
}
