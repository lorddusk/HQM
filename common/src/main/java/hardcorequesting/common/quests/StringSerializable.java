package hardcorequesting.common.quests;

public interface StringSerializable {
    String saveToString();
    
    void clear();
    
    void loadFromString(String string);
    
    default void clearAndLoad(String string) {
        clear();
        loadFromString(string);
    }
}