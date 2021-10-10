package hardcorequesting.common.quests;

public interface StringSerializable {
    String saveToString();
    
    void clear();
    
    void loadFromString(String string);
}