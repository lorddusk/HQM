package hardcorequesting.event;

import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;

public class WorldEventListener {
    
    public static void onLoad(RegistryKey<World> worldRegistryKey, ServerWorld world) {
        if (!world.isClient && world.getRegistryKey().equals(World.OVERWORLD)) {
            QuestLine.reset();
            if (Quest.useDefault) { // reloads all quest lines (rewrites existing ones and creates new ones, does not remove old ones)
                QuestLine.copyDefaults(getWorldPath(world));
            }
            QuestLine.loadWorldData(getWorldPath(world), world.isClient);
        }
    }
    
    public static void onSave(ServerWorld world, ProgressListener listener, boolean flush) {
        if (!world.isClient && world.getRegistryKey().equals(World.OVERWORLD)) {
            QuestLine.saveAll();
            System.out.println("adwad");
        }
    }
    
    public static void onCreate(ServerWorld world) {
        if (!world.isClient && world.getRegistryKey().equals(World.OVERWORLD)) {
            QuestLine.reset();
            QuestLine.copyDefaults(getWorldPath(world));
            QuestLine.loadWorldData(getWorldPath(world), world.isClient); // Reload because the defaults wouldn't have been loaded in
        }
    }
    
    private static File getWorldPath(ServerWorld world) {
        return world.getServer().session.getDirectory(WorldSavePath.ROOT).toAbsolutePath().normalize().toFile();
    }
    
}
