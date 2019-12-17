package hqm;

import net.minecraftforge.fml.common.Mod;

/**
 * @author canitzp
 */
@Mod.EventBusSubscriber
public class Registry {

    /* todo something with 1.15 blablabla
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
    
     */

}
