package hardcorequesting.quests;

import hardcorequesting.OreDictionaryHelper;
import hardcorequesting.Translator;
import net.minecraft.item.ItemStack;

public enum ItemPrecision {
    PRECISE("precise") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem() && item1.getItemDamage() == item2.getItemDamage() && ItemStack.areItemStackTagsEqual(item1, item2);
        }
    },
    NBT_FUZZY("nbtFuzzy") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem() && item1.getItemDamage() == item2.getItemDamage();
        }
    },
    FUZZY("fuzzy") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem();
        }
    },
    ORE_DICTIONARY("oreDict") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2)
        {
            return OreDictionaryHelper.match(item1, item2) || PRECISE.same(item1, item2);
        }
    };

    private String id;
    private ItemPrecision(String name) {
        this.id = name;
    }

    protected abstract boolean same(ItemStack item1, ItemStack item2);
    public final boolean areItemsSame(ItemStack item1, ItemStack item2) {
        return item1 == null && item2 == null || item1 != null && item2 != null && same(item1, item2);
    }

    public String getName()
    {
        return Translator.translate("hqm.precision." + id);
    }

    @Override
    public String toString() {
        return id;
    }
}
