package hardcorequesting.common.io;

import java.io.FileFilter;
import java.util.stream.Stream;

public interface DataManager {
    FileProvider resolve(String name);
    
    Stream<String> resolveAll(FileFilter filter);
    
    FileProvider resolveData(String name);
}
