package hardcorequesting.event;

import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;

public class WorldEventListener {
    
    public static void onLoad(WorldSaveHandler saveHandler, ServerWorld world) {
        if (!world.isClient && world.dimension.getType() == DimensionType.OVERWORLD) {
            QuestLine.reset();
            if (Quest.useDefault) { // reloads all quest lines (rewrites existing ones and creates new ones, does not remove old ones)
                QuestLine.copyDefaults(getWorldPath(saveHandler, world));
            }
            QuestLine.loadWorldData(getWorldPath(saveHandler, world), world.isClient);
        }
    }
    
    public static void onSave(ServerWorld world) {
        if (!world.isClient && world.dimension.getType() == DimensionType.OVERWORLD) {
            QuestLine.saveAll();
        }
    }
    
    public static void onCreate(WorldSaveHandler saveHandler, ServerWorld world) {
        if (!world.isClient && world.dimension.getType() == DimensionType.OVERWORLD) {
            QuestLine.reset();
            QuestLine.copyDefaults(getWorldPath(saveHandler, world));
            QuestLine.loadWorldData(getWorldPath(saveHandler, world), world.isClient); // Reload because the defaults wouldn't have been loaded in
        }
    }
    
    private static File getWorldPath(WorldSaveHandler saveHandler, ServerWorld world) {
        return saveHandler.getWorldDir();
    }
    
}
