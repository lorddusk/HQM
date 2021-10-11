package hardcorequesting.common.quests;

import hardcorequesting.common.io.DataReader;
import hardcorequesting.common.io.DataWriter;

public abstract class SimpleSerializable implements StringSerializable, Serializable {
    
    public SimpleSerializable() {
    }
    
    public abstract String filePath();
    
    @Override
    public final void save(DataWriter writer) {
        writer.write(filePath(), saveToString());
    }
    
    @Override
    public final void load(DataReader reader) {
        clear();
        reader.read(filePath()).ifPresent(this::loadFromString);
    }
}
