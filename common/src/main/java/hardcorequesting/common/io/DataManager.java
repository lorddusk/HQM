package hardcorequesting.common.io;

import java.util.Optional;

@Deprecated
public interface DataManager extends DataReader, DataWriter {
    @Override
    default Optional<String> read(String name) {
        return resolve(name).get();
    }
    
    @Override
    default Optional<String> readData(String name) {
        return resolveData(name).get();
    }
    
    @Override
    default void write(String name, String text) {
        resolve(name).set(text);
    }
    
    @Override
    default void writeData(String name, String text) {
        resolveData(name).set(text);
    }
    
    FileProvider resolve(String name);
    
    FileProvider resolveData(String name);
}
