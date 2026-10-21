package hardcorequesting.fabric.test;

import hardcorequesting.common.io.DataReader;
import hardcorequesting.common.io.DataWriter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Book files kept in memory, so tests can load and save without touching the disk.
 */
public final class MemoryData implements DataReader, DataWriter {
    private final Map<String, String> files = new TreeMap<>();

    @Override
    public Optional<String> read(String name) {
        return Optional.ofNullable(files.get(name));
    }

    /**
     * Only the files directly in the root, like the directory listing of the file based reader.
     */
    @Override
    public Stream<String> readAll(DirectoryStream.Filter<Path> filter) {
        return files.entrySet().stream()
                .filter(entry -> !entry.getKey().contains("/"))
                .filter(entry -> {
                    try {
                        return filter.accept(Path.of(entry.getKey()));
                    } catch (IOException e) {
                        return false;
                    }
                })
                .map(Map.Entry::getValue);
    }

    @Override
    public void write(String name, String text) {
        files.put(name, text);
    }

    public MemoryData with(String name, String text) {
        write(name, text);
        return this;
    }

    /**
     * @return all files by name, sorted
     */
    public Map<String, String> files() {
        return files;
    }
}
