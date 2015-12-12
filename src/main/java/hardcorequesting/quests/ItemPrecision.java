package hardcorequesting.quests;

import com.google.common.collect.ImmutableList;
import hardcorequesting.OreDictionaryHelper;
import hardcorequesting.Translator;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class ItemPrecision {
    public static final ItemPrecision PRECISE = new ItemPrecision("precise") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem() && item1.getItemDamage() == item2.getItemDamage() && ItemStack.areItemStackTagsEqual(item1, item2);
        }
    };
    public static final ItemPrecision NBT_FUZZY = new ItemPrecision("nbtFuzzy") {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem() && item1.getItemDamage() == item2.getItemDamage();
        }
    };
    public static final ItemPrecision FUZZY = new ItemPrecision("fuzzy", true) {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return item1.getItem() == item2.getItem();
        }

        @Override
        public ItemStack[] getPermutations(ItemStack stack) {
            List<ItemStack> items = new ArrayList<>();
            stack.getItem().getSubItems(stack.getItem(), null, items);
            return items.toArray(new ItemStack[items.size()]);
        }
    };
    public static final ItemPrecision ORE_DICTIONARY = new ItemPrecision("oreDict", true) {
        @Override
        protected boolean same(ItemStack item1, ItemStack item2) {
            return OreDictionaryHelper.match(item1, item2) || PRECISE.same(item1, item2);
        }

        @Override
        public ItemStack[] getPermutations(ItemStack stack) {
            return OreDictionaryHelper.getPermutations(stack);
        }
    };

    private String tag;

    public ItemPrecision(String tag) {
        this.tag = tag;
    }

    public ItemPrecision(String tag, boolean hasPermutations) {
        this(tag);
        this.hasPermutations = hasPermutations;
    }

    protected abstract boolean same(ItemStack item1, ItemStack item2);

    public final boolean areItemsSame(ItemStack item1, ItemStack item2) {
        return item1 == null && item2 == null || item1 != null && item2 != null && same(item1, item2);
    }

    @Override
    public String toString() {
        return tag;
    }

    public String getLocalizationTag() {
        return "hqm.precision." + tag;
    }

    public String getName() {
        return Translator.translate(getLocalizationTag());
    }

    protected boolean hasPermutations = false;

    public boolean hasPermutations() {
        return hasPermutations;
    }

    public ItemStack[] getPermutations(ItemStack stack) {
        return new ItemStack[0];
    }

    // Registry things

    private static final LinkedHashMap<String, ItemPrecision> precisionTypes;

    static {
        // Need to do this here to avoid NPE
        precisionTypes = new LinkedHashMap<>();
        // Using the former names of the enum entries for backwards compatibility
        registerPrecisionType("PRECISE", PRECISE);
        registerPrecisionType("NBT_FUZZY", NBT_FUZZY);
        registerPrecisionType("FUZZY", FUZZY);
        registerPrecisionType("ORE_DICTIONARY", ORE_DICTIONARY);
    }

    public static boolean registerPrecisionType(String uniqueID, ItemPrecision p) {
        if (uniqueID == null || p == null) {
            return false;
        }
        if (!precisionTypes.containsKey(uniqueID)) {
            precisionTypes.put(uniqueID, p);
            return true;
        }
        return false;
    }

    public static ImmutableList<ItemPrecision> getPrecisionTypes() {
        return ImmutableList.copyOf(precisionTypes.values());
    }

    public static ItemPrecision getPrecisionType(String uniqueID) {
        return precisionTypes.containsKey(uniqueID) ? precisionTypes.get(uniqueID) : PRECISE;
    }

    // For backwards compatibility

    public static ItemPrecision getOldPrecisionType(int ordinal) {
        switch (ordinal) {
            case 1: {
                return NBT_FUZZY;
            }
            case 2: {
                return FUZZY;
            }
            case 3: {
                return ORE_DICTIONARY;
            }
            default: {
                return PRECISE;
            }
        }
    }

    public static String getUniqueID(ItemPrecision p) {
        for (Map.Entry<String, ItemPrecision> entry : precisionTypes.entrySet()) {
            if (entry.getValue() == p) {
                return entry.getKey();
            }
        }
        return "PRECISE";
    }
}
