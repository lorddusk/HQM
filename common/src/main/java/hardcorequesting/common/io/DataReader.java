package hardcorequesting.common.io;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public interface DataReader {
    Optional<String> read(String name);
    
    default Stream<String> readAll(DirectoryStream.Filter<Path> filter) {
        return Stream.empty();
    }
}
