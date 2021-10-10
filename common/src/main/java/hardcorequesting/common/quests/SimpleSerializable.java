package hardcorequesting.common.quests;

import hardcorequesting.common.io.DataReader;
import hardcorequesting.common.io.DataWriter;

import java.util.Optional;

public abstract class SimpleSerializable implements StringSerializable, Serializable {
    protected final QuestLine parent;
    
    public SimpleSerializable(QuestLine parent) {
        this.parent = parent;
    }
    
    public abstract String filePath();
    
    @Override
    public final void save(DataWriter writer) {
        if (isData()) {
            writer.writeData(filePath(), saveToString());
        } else {
            writer.write(filePath(), saveToString());
        }
    }
    
    @Override
    public final void load(DataReader reader) {
        loadFromString(Optional.empty());
        Optional<String> str;
        if (isData()) {
            str = reader.readData(filePath());
        } else {
            str = reader.read(filePath());
        }
        loadFromString(str);
    }
}
