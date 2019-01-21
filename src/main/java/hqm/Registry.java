package hqm;

import hqm.item.ItemQuestbook;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = HQM.MODID)
public class Registry{
    
    public static ItemQuestbook itemQuestBook;
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event){
        event.getRegistry().register(itemQuestBook = new ItemQuestbook());
    }
    
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event){
        ModelLoader.setCustomModelResourceLocation(itemQuestBook, 0, new ModelResourceLocation(itemQuestBook.getRegistryName(), "inventory"));
    }
    
}
