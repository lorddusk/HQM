package hardcorequesting.common.quests;

public interface Serializable {
    boolean isData();
    
    void save();
    
    void load();
}
