package hardcorequesting;

import hardcorequesting.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HQMTab extends CreativeTabs {

    public HQMTab() {
        super("hqm");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack() {
        // the Icon of the creative Tab
        return new ItemStack(ModItems.book, 1, 0);
    }

    @Override
    public ItemStack getTabIconItem() {
        return getIconItemStack();
    }
}
