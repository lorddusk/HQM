package hardcorequesting.quests;

import com.google.common.collect.ImmutableList;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.util.NBTCompareUtil;
import hardcorequesting.util.OreDictionaryHelper;
import hardcorequesting.util.Translator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ItemPrecision {

    public static final ItemPrecision PRECISE = new ItemPrecision("precise") {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return stack1.getItem() == stack2.getItem() && stack1.getItemDamage() == stack2.getItemDamage() && ItemStack.areItemStackTagsEqual(stack1, stack2);
        }
    };
    public static final ItemPrecision NBT_FUZZY = new ItemPrecision("nbtFuzzy") {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return stack1.getItem() == stack2.getItem() && stack1.getItemDamage() == stack2.getItemDamage();
        }
    };
    public static final ItemPrecision FUZZY = new ItemPrecision("fuzzy", true) {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return stack1.getItem() == stack2.getItem();
        }

        @Override
        public ItemStack[] getPermutations(ItemStack stack) {
            NonNullList<ItemStack> items = NonNullList.create();
            stack.getItem().getSubItems(null, items);
            return items.toArray(new ItemStack[items.size()]);
        }
    };
    public static final ItemPrecision ORE_DICTIONARY = new ItemPrecision("oreDict", true) {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return OreDictionaryHelper.match(stack1, stack2) || PRECISE.same(stack1, stack2);
        }

        @Override
        public ItemStack[] getPermutations(ItemStack stack) {
            return OreDictionaryHelper.getPermutations(stack);
        }
    };
    public static final ItemPrecision NBT_SUBSET = new ItemPrecision("nbtSubset", false) {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            final NBTTagCompound ref = (stack2.hasTagCompound()) ? stack2.getTagCompound() : null;
            final NBTTagCompound checking = (stack1.hasTagCompound()) ? stack1.getTagCompound() : null;

            if (ref != null) {
                for (String key : HQMConfig.NBT_SUBSET_FILTER) {
                    ref.removeTag(key);
                }
            }

            boolean nbt = NBTCompareUtil.arePartiallySimilar(checking, ref);

            if (!nbt) return false;

            if (!stack2.isItemStackDamageable() && stack1.getMetadata() != stack2.getMetadata()) return false;

            return stack1.getItem() == stack2.getItem();
        }
    };
    private static final LinkedHashMap<String, ItemPrecision> precisionTypes;

    static {
        // Need to do this here to avoid NPE
        precisionTypes = new LinkedHashMap<>();
        // Using the former names of the enum entries for backwards compatibility
        registerPrecisionType("PRECISE", PRECISE);
        registerPrecisionType("NBT_FUZZY", NBT_FUZZY);
        registerPrecisionType("FUZZY", FUZZY);
        registerPrecisionType("ORE_DICTIONARY", ORE_DICTIONARY);
        registerPrecisionType("NBT_SUBSET", NBT_SUBSET);
    }

    protected boolean hasPermutations = false;
    private String tag;

    public ItemPrecision(String tag) {
        this.tag = tag;
    }

    public ItemPrecision(String tag, boolean hasPermutations) {
        this(tag);
        this.hasPermutations = hasPermutations;
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

    /* This code is never called
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
    } */

    public static String getUniqueID(ItemPrecision p) {
        for (Map.Entry<String, ItemPrecision> entry : precisionTypes.entrySet()) {
            if (entry.getValue() == p) {
                return entry.getKey();
            }
        }
        return "PRECISE";
    }

    // Registry things

    protected abstract boolean same(ItemStack stack1, ItemStack stack2);

    public final boolean areItemsSame(ItemStack stack1, ItemStack stack2) {
        return stack1.isEmpty() && stack2.isEmpty() || !stack1.isEmpty() && !stack2.isEmpty() && same(stack1, stack2);
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

    // For backwards compatibility

    public boolean hasPermutations() {
        return hasPermutations;
    }

    public ItemStack[] getPermutations(ItemStack stack) {
        return new ItemStack[0];
    }
}
