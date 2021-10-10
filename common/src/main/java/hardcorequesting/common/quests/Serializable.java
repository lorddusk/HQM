package hardcorequesting.common.quests;

import hardcorequesting.common.io.DataManager;

public interface Serializable {
    boolean isData();
    
    void save(DataManager dataManager);
    
    void load(DataManager dataManager);
}
