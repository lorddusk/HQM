package hardcorequesting;

import hardcorequesting.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class HQMTab extends CreativeTabs {
    
    public HQMTab() {
        super("hqm");
    }

    @Nonnull
    @Override
    public ItemStack createIcon(){
        return new ItemStack(ModItems.book, 1, 0);
    }
}
