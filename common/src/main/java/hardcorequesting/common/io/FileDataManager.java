package hardcorequesting.common.io;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class FileDataManager implements DataReader, DataWriter {
    
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
    public Stream<String> readAll(FileFilter filter) {
        //TODO use different function that doesn't catch IOException to have access to more information if an I/O error occurs.
        File[] files = path.toFile().listFiles(filter);
        return files == null ? Stream.empty() : Arrays.stream(files)
                .flatMap(file -> SaveHandler.load(file).stream());
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
