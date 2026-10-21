package hardcorequesting.common.quests;

import hardcorequesting.common.HardcoreQuestingCore;
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
        reader.read(filePath()).ifPresent(text -> {
            try {
                loadFromString(text);
            } catch (RuntimeException e) {
                // The rest has to load without it. It is also cleared again, as loading may have stopped halfway.
                HardcoreQuestingCore.LOGGER.error("Could not read %s, continuing without its content", filePath(), e);
                clear();
                keepUnreadable(reader, filePath(), text);
            }
        });
    }
    
    /**
     * Keeps the content that could not be read next to the file, as the file is overwritten the next time the data is saved.
     */
    static void keepUnreadable(DataReader reader, String filePath, String text) {
        if (reader instanceof DataWriter writer) {
            String name = filePath + ".corrupt-" + System.currentTimeMillis();
            writer.write(name, text);
            HardcoreQuestingCore.LOGGER.error("The content that could not be read was kept in %s", name);
        }
    }
}
