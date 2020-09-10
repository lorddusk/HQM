package hardcorequesting.quests;

public interface Serializable {
    boolean isData();
    
    void save();
    
    void load();
}
