package hqm.events;

import hqm.HQM;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = HQM.MODID)
public class WorldEvents{
    
    @SubscribeEvent
    public static void loadWorld(WorldEvent.Load event){
    
    }
    
    @SubscribeEvent
    public static void saveWorld(WorldEvent.Save event){
    
    }
    
}
