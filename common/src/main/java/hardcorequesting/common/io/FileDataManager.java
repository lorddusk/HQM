package hardcorequesting.common.io;

import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class FileDataManager implements DataManager {
    
    private final Path basePath;
    @Nullable
    private final Path dataPath;
    
    public static FileDataManager createForServer(MinecraftServer server) {
        Path hqm = getWorldPath(server).resolve("hqm");
        return new FileDataManager(HardcoreQuestingCore.packDir, hqm);
    }
    
    public static FileDataManager createForExport() {
        return new FileDataManager(HardcoreQuestingCore.packDir, null);
    }
    
    private static Path getWorldPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }
    
    public FileDataManager(Path basePath, @Nullable Path dataPath) {
        this.basePath = basePath;
        this.dataPath = dataPath;
    
        try {
            if (!Files.exists(this.basePath)) {
                Files.createDirectories(this.basePath);
            }
            if (dataPath != null && !Files.exists(this.dataPath)) {
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
    public Stream<String> readAll(FileFilter filter) {
        //TODO use different function that doesn't catch IOException to have access to more information if an I/O error occurs.
        File[] files = basePath.toFile().listFiles(filter);
        return files == null ? Stream.empty() : Arrays.stream(files)
                .flatMap(file -> SaveHandler.load(file).stream());
    }
    
    @Override
    public FileProvider resolveData(String name) {
        return dataPath == null ? FileProvider.EMPTY : new FileProvider.PathProvider(dataPath.resolve(name));
    }
    
    @Override
    public void writeData(String name, String text) {
        if (dataPath != null)
            resolve(name).set(text);
    }
    
    @Override
    public String toString() {
        return "File data";
    }
}
