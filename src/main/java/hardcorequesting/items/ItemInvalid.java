package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import net.minecraft.item.Item;

/**
 * @author Tim
 */
public class ItemInvalid extends Item {

    public ItemInvalid() {
        super();
        this.setMaxStackSize(1);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setRegistryName(ItemInfo.INVALID_UNLOCALIZED_NAME);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.INVALID_UNLOCALIZED_NAME);
    }
}
