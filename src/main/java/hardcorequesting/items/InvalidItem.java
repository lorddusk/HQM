package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import net.minecraft.item.Item;

/**
 * @author Tim
 */
public class InvalidItem extends Item {
    
    public InvalidItem() {
        super(new Item.Settings()
                .maxCount(1)
                .group(HardcoreQuesting.HQMTab));
    }
}
