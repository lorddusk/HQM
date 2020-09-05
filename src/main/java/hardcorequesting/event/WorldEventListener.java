package hardcorequesting.event;

import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public class WorldEventListener {
    
    public static void onLoad(ResourceKey<Level> worldRegistryKey, ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine.reset();
            if (Quest.useDefault) { // reloads all quest lines (rewrites existing ones and creates new ones, does not remove old ones)
                QuestLine.copyDefaults(getWorldPath(world));
            }
            QuestLine.loadWorldData(getWorldPath(world), world.isClientSide);
        }
    }
    
    public static void onSave(ServerLevel world, ProgressListener listener, boolean flush) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine.saveAll();
            System.out.println("adwad");
        }
    }
    
    public static void onCreate(ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine.reset();
            QuestLine.copyDefaults(getWorldPath(world));
            QuestLine.loadWorldData(getWorldPath(world), world.isClientSide); // Reload because the defaults wouldn't have been loaded in
        }
    }
    
    private static File getWorldPath(ServerLevel world) {
        return world.getServer().storageSource.getLevelPath(LevelResource.ROOT).toAbsolutePath().normalize().toFile();
    }
    
}
