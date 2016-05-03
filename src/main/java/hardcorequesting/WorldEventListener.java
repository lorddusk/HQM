package hardcorequesting;

import hardcorequesting.quests.QuestLine;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class WorldEventListener {

    public WorldEventListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            QuestLine.reset();
            WorldServer world = (WorldServer) event.getWorld();
            QuestLine.loadWorldData(getWorldPath(world));
            QuestingData.load(getWorldPath(world), world);
        }
    }

    @SubscribeEvent
    public void onSave(WorldEvent.Save event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            WorldServer world = (WorldServer) event.getWorld();
            QuestingData.save(getWorldPath(world), world);
        }
    }

    private File getWorldPath(WorldServer world) {
        return world.getChunkSaveLocation();
    }

}
