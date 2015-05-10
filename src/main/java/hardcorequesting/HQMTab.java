package hardcorequesting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HQMTab extends CreativeTabs {

	public HQMTab(int par1, String par2Str) {
		super(par1, par2Str);
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack(){
	// the Icon of the creative Tab
	return new ItemStack(ModItems.book, 1, 0);
	}

    @Override
    public Item getTabIconItem() {
        return null;
    }

    public String getTranslatedTabLabel(){
	// this is the name of the Tab 
	return "Hardcore Questing Mode"; 
	}


}
