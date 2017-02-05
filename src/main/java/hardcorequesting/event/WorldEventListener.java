package hardcorequesting.event;

import hardcorequesting.quests.QuestLine;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class WorldEventListener {

    public WorldEventListener() {
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            QuestLine.reset();
            WorldServer world = (WorldServer) event.getWorld();
            QuestLine.loadWorldData(getWorldPath(world), world.isRemote);
        }
    }

    @SubscribeEvent
    public void onSave(WorldEvent.Save event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            QuestLine.saveAll();
        }
    }

    @SubscribeEvent
    public void onCreate(WorldEvent.CreateSpawnPosition event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            WorldServer world = (WorldServer) event.getWorld();
            QuestLine.copyDefaults(getWorldPath(world));
        }
    }

    private File getWorldPath(WorldServer world) {
        return world.getChunkSaveLocation();
    }

}
