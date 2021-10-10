package hardcorequesting.common.io;

import java.io.FileFilter;
import java.util.Optional;
import java.util.stream.Stream;

public interface DataReader {
    Optional<String> read(String name);
    
    default Stream<String> readAll(FileFilter filter) {
        return Stream.empty();
    }
}
