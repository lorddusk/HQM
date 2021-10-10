package hardcorequesting.common.io;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FileDataManager implements DataReader, DataWriter {
    private static final Logger LOGGER = LogManager.getLogger("Hardcore Questing Mode");
    
    private final Path path;
    
    public static FileDataManager createForWorldData(MinecraftServer server) {
        Path hqm = getWorldPath(server).resolve("hqm");
        return new FileDataManager(hqm);
    }
    
    private static Path getWorldPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }
    
    public FileDataManager(Path path) {
        this.path = path;
    
        try {
            if (!Files.exists(this.path)) {
                Files.createDirectories(this.path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Optional<String> read(String name) {
        return SaveHandler.load(path.resolve(name));
    }
    
    @Override
    public Stream<String> readAll(DirectoryStream.Filter<Path> filter) {
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, filter))
        {
            List<Path> paths = new ArrayList<>();
            stream.forEach(paths::add);
            
            return paths.stream().flatMap(file -> SaveHandler.load(file).stream());
        } catch (IOException e)
        {
            LOGGER.warn("File search failed.", e);
            return Stream.empty();
        }
    }
    
    @Override
    public void write(String name, String text) {
        SaveHandler.save(path.resolve(name), text);
    }
    
    @Override
    public String toString() {
        return "File data";
    }
}
