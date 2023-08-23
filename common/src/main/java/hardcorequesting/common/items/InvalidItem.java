package hardcorequesting.common.items;

import net.minecraft.world.item.Item;

/**
 * @author Tim
 */
public class InvalidItem extends Item {
    public InvalidItem() {
        super(new Item.Properties()
                .stacksTo(1));
    }
}
