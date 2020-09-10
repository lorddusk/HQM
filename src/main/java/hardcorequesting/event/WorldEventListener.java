package hardcorequesting.event;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.quests.QuestLine;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.Optional;

public class WorldEventListener {
    public static void onLoad(ResourceKey<Level> worldRegistryKey, ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            Path hqm = getWorldPath(world).resolve("hqm");
            QuestLine questLine = QuestLine.reset(Optional.of(HardcoreQuesting.packDir), Optional.of(hqm));
            questLine.loadAll();
        }
    }
    
    public static void onSave(ServerLevel world, ProgressListener listener, boolean flush) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine.getActiveQuestLine().saveData();
        }
    }
    
    private static Path getWorldPath(ServerLevel world) {
        return world.getServer().storageSource.getLevelPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }
    
}
