package hardcorequesting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

public class OreDictionaryHelper {
    public static boolean match(ItemStack itemStack1, ItemStack itemStack2) {
        int[] ids1 = OreDictionary.getOreIDs(itemStack1);
        int[] ids2 = OreDictionary.getOreIDs(itemStack2);
        for (int id1 : ids1)
            for (int id2 : ids2)
                if (id1 == id2)
                    return true;
        return false;
    }

    public static List<ItemStack> getPermutationsList(ItemStack itemStack) {
        List<ItemStack> permutations = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(itemStack))
            permutations.addAll(getPermutationsList(OreDictionary.getOreName(id)));
        if (permutations.size() == 0)
            permutations.add(itemStack);
        return stripWildcardDamage(permutations);
    }

    public static ItemStack[] getPermutations(ItemStack itemStack) {
        List<ItemStack> permutations = getPermutationsList(itemStack);
        return permutations.toArray(new ItemStack[permutations.size()]);
    }

    public static List<ItemStack> getPermutationsList(String oreName) {
        return stripWildcardDamage(OreDictionary.getOres(oreName));
    }

    public static ItemStack[] getPermutations(String oreName) {
        List<ItemStack> permutations = getPermutationsList(oreName);
        return permutations.toArray(new ItemStack[permutations.size()]);
    }

    public static String[] getOreNames(ItemStack itemStack) {
        List<String> names = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(itemStack))
            names.add(OreDictionary.getOreName(id));
        return names.toArray(new String[names.size()]);
    }

    private static List<ItemStack> stripWildcardDamage(List<ItemStack> list) {
        List<ItemStack> result = new ArrayList<>(list.size());
        for (ItemStack itemStack : list)
            result.add(new ItemStack(itemStack.getItem(), itemStack.stackSize, itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : itemStack.getItemDamage()));
        return result;
    }
}
