package hqm.item;

import hqm.HQM;
import hqm.Registry;
import net.minecraft.item.Item;
import net.minecraft.util.NonNullList;

import java.util.function.Supplier;

/**
 * @author canitzp
 */
public class ItemBase<T extends ItemBase> extends Item {

    public static final NonNullList<ItemBase> ITEMS = NonNullList.create();

    public ItemBase(String name, Supplier<Properties> s){
        super(s.get().group(Registry.TAB));
        this.setRegistryName(HQM.MODID, name);
    }

    public T register(){
        ITEMS.add(this);
        return (T) this;
    }

}
