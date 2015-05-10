package hardcorequesting.quests;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public enum ItemPrecision {
    PRECISE("Precise detection") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem() && item1.getItemDamage() == item2.getItemDamage() && ItemStack.areItemStackTagsEqual(item1, item2);
        }
    },
    NBT_FUZZY("NBT independent detection") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem() && item1.getItemDamage() == item2.getItemDamage();
        }
    },
    FUZZY("Fuzzy detection") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem();
        }
    },
    ORE_DICTIONARY("Ore dictionary detection") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            int id1 = OreDictionary.getOreID(item1);
            if (id1 != -1) {
                int id2 = OreDictionary.getOreID(item2);
                return id1 == id2;
            }else{
                return PRECISE.same(item1, item2);
            }
        }
    };

    private String name;
    private ItemPrecision(String name) {
        this.name = name;
    }

    protected abstract boolean same(ItemStack item1, ItemStack item2);
    public final boolean areItemsSame(ItemStack item1, ItemStack item2) {
        return item1 == null && item2 == null || item1 != null && item2 != null && same(item1, item2);
    }


    @Override
    public String toString() {
        return name;
    }
}
