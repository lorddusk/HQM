package hqm;

import hqm.item.ItemBase;
import hqm.item.ItemQuestBook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author canitzp
 */
@Mod.EventBusSubscriber(modid = HQM.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registry {

    public static ItemGroup TAB = new ItemGroup(HQM.MODID) {
        @Override
        public ItemStack createIcon(){
            return new ItemStack(questBook);
        }
    };

    public static ItemQuestBook questBook = new ItemQuestBook().register();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> reg){
        ItemBase.ITEMS.forEach(itemBase -> reg.getRegistry().register(itemBase));
    }
    

}
