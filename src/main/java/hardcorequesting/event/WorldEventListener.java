package hardcorequesting.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import hardcorequesting.quests.QuestLine;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.io.File;

public class WorldEventListener {

    public WorldEventListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            QuestLine.reset();
            WorldServer world = (WorldServer) event.world;
            QuestLine.loadWorldData(getWorldPath(world), world.isRemote);
        }
    }

    @SubscribeEvent
    public void onSave(WorldEvent.Save event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            QuestLine.saveAll();
        }
    }

    @SubscribeEvent
    public void onCreate(WorldEvent.CreateSpawnPosition event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            WorldServer world = (WorldServer) event.world;
            QuestLine.copyDefaults(getWorldPath(world));
        }
    }

    private File getWorldPath(WorldServer world) {
        return world.getChunkSaveLocation();
    }

}
