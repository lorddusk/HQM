package hardcorequesting.common.quests;

import com.google.common.collect.ImmutableList;
import hardcorequesting.common.HardcoreQuestingCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ItemPrecision {
    
    public static final ItemPrecision PRECISE = new ItemPrecision("precise") {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return stack1.getItem() == stack2.getItem() && ItemStack.tagMatches(stack1, stack2);
        }
    };
    public static final ItemPrecision NBT_FUZZY = new ItemPrecision("nbtFuzzy") {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return stack1.getItem() == stack2.getItem();
        }
    };
    public static final ItemPrecision FUZZY = new ItemPrecision("fuzzy", true) {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            return stack1.getItem() == stack2.getItem();
        }
    };
    public static final ItemPrecision TAG_NBT_FUZZY = new ItemPrecision("tagNbtFuzzy", true) {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            if (stack1.getItem() == stack2.getItem())
                return true;
            for (Map.Entry<ResourceLocation, Tag<Item>> entry : HardcoreQuestingCore.getServer().getTags().getItems().getAllTags().entrySet()) {
                if (entry.getValue().contains(stack1.getItem()) && entry.getValue().contains(stack2.getItem()))
                    return true;
            }
            return false;
        }
    };
    public static final ItemPrecision TAG_FUZZY = new ItemPrecision("tagFuzzy", true) {
        @Override
        protected boolean same(ItemStack stack1, ItemStack stack2) {
            if (stack1.getItem() == stack2.getItem())
                return true;
            for (Map.Entry<ResourceLocation, Tag<Item>> entry : HardcoreQuestingCore.getServer().getTags().getItems().getAllTags().entrySet()) {
                if (entry.getValue().contains(stack1.getItem()) && entry.getValue().contains(stack2.getItem()))
                    return false;
            }
            return false;
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
        registerPrecisionType("TAG_NBT_FUZZY", TAG_NBT_FUZZY);
        registerPrecisionType("TAG_FUZZY", TAG_FUZZY);
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
    
    @Environment(EnvType.CLIENT)
    public String getName() {
        return I18n.get(getLocalizationTag());
    }
    
    // For backwards compatibility
    
    public boolean hasPermutations() {
        return hasPermutations;
    }
    
    public ItemStack[] getPermutations(ItemStack stack) {
        return new ItemStack[0];
    }
}
