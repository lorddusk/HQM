package hardcorequesting.common.event;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.FileDataManager;
import hardcorequesting.common.quests.QuestLine;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

public class WorldEventListener {
    public static void onLoad(ResourceKey<Level> worldRegistryKey, ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            Path hqm = getWorldPath(world).resolve("hqm");
            QuestLine questLine = QuestLine.reset(new FileDataManager(HardcoreQuestingCore.packDir, hqm));
            questLine.loadAll();
        }
    }
    
    public static void onSave(ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine.getActiveQuestLine().saveData();
        }
    }
    
    private static Path getWorldPath(ServerLevel world) {
        return world.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
    }
    
}
