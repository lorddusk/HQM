package hardcorequesting.common.quests;

import java.util.Optional;

public interface StringSerializable {
    String saveToString();
    
    void loadFromString(Optional<String> string);
}