package hardcorequesting.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class FileDataManager implements DataManager {
    
    private final Path basePath, dataPath;
    
    public FileDataManager(Path basePath, Path dataPath) {
        this.basePath = basePath;
        this.dataPath = dataPath;
    
        try {
            if (!Files.exists(this.basePath)) {
                Files.createDirectories(this.basePath);
            }
            if (!Files.exists(this.dataPath)) {
                Files.createDirectories(this.dataPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public FileProvider resolve(String name) {
        return new FileProvider.PathProvider(basePath.resolve(name));
    }
    
    @Override
    public Stream<String> resolveAll(FileFilter filter) {
        //TODO use different function that doesn't catch IOException to have access to more information if an I/O error occurs.
        File[] files = basePath.toFile().listFiles(filter);
        return files == null ? Stream.empty() : Arrays.stream(files)
                .flatMap(file -> SaveHandler.load(file).stream());
    }
    
    @Override
    public FileProvider resolveData(String name) {
        return new FileProvider.PathProvider(dataPath.resolve(name));
    }
}
