package hqm;

import hqm.api.IQuestbook;
import hqm.io.IOHandler;
import hqm.net.NetManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;

@Mod(modid = HQM.MODID, name = HQM.MODNAME, version = HQM.MODVERSION)
public class HQM{
    
    public static final String MODID = "hqm";
    public static final String MODNAME = "Hardcore Questing Mode";
    public static final String MODVERSION = "@VERSION@";
    
    public static final CreativeTabs HQM_TAB = new CreativeTabs(MODID){
        @Nonnull
        @Override
        public ItemStack createIcon(){
            return new ItemStack(Registry.itemQuestBook, 1, 0);
        }
    
        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items){
            items.add(new ItemStack(Registry.itemQuestBook));
            for(IQuestbook questbook : IOHandler.getAllQuestbooks()){
                items.addAll(questbook.getItemStacks());
            }
        }
    };
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        NetManager.init();
        IOHandler.readQuestbooks(true); // read all questbooks from files and add them to the game logic
    }
    
}
