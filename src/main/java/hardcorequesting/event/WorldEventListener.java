package hardcorequesting.event;

import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class WorldEventListener {

    @SubscribeEvent
    public static void onLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            QuestLine.reset();
            WorldServer world = (WorldServer) event.getWorld();
            if (Quest.useDefault) { // reloads all quest lines (rewrites existing ones and creates new ones, does not remove old ones)
                QuestLine.copyDefaults(getWorldPath(world));
            }
            QuestLine.loadWorldData(getWorldPath(world), world.isRemote);
        }
    }

    @SubscribeEvent
    public static void onSave(WorldEvent.Save event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            QuestLine.saveAll();
        }
    }

    @SubscribeEvent
    public static void onCreate(WorldEvent.CreateSpawnPosition event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            WorldServer world = (WorldServer) event.getWorld();
            QuestLine.reset();
            QuestLine.copyDefaults(getWorldPath(world));
            QuestLine.loadWorldData(getWorldPath(world), world.isRemote); // Reload because the defaults wouldn't have been loaded in
        }
    }

    private static File getWorldPath(WorldServer world) {
        return world.getChunkSaveLocation();
    }

}
