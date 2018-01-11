package hqm;

import hqm.item.ItemBase;
import hqm.item.ItemQuestBook;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author canitzp
 */
@Mod.EventBusSubscriber
public class Registry {

    public static CreativeTabs TAB = new CreativeTabs(HQM.MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(questBook);
        }
    };

    public static ItemQuestBook questBook = new ItemQuestBook().register();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> reg){
        ItemBase.ITEMS.forEach(itemBase -> reg.getRegistry().register(itemBase));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void bakeEvent(ModelBakeEvent event){
        ItemBase.ITEMS.forEach(item -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory")));
    }

}
