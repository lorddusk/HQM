package hardcorequesting.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//import net.minecraft.client.renderer.texture.IIconRegister;

/**
 * Created by Tim on 5/10/2015.
 */
public class ItemInvalid extends Item {

    public ItemInvalid() {
        super();
        this.setMaxStackSize(1);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.INVALID_UNLOCALIZED_NAME);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack);
    }

}
