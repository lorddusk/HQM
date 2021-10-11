package hardcorequesting.common.quests;

import hardcorequesting.common.io.DataReader;
import hardcorequesting.common.io.DataWriter;

public interface Serializable {
    boolean isData();
    
    void save(DataWriter writer);
    
    void load(DataReader reader);
}
