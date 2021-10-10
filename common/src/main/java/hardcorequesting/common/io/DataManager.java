package hardcorequesting.common.io;

import java.io.FileFilter;
import java.util.stream.Stream;

public interface DataManager {
    FileProvider resolve(String name);
    
    default Stream<String> resolveAll(FileFilter filter) {
        return Stream.empty();
    }
    
    FileProvider resolveData(String name);
}
