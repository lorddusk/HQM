package hardcorequesting.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class OreDictionaryHelper {

    public static boolean match(ItemStack stack1, ItemStack stack2) {
        int[] ids1 = OreDictionary.getOreIDs(stack1);
        int[] ids2 = OreDictionary.getOreIDs(stack2);
        for (int id1 : ids1)
            for (int id2 : ids2)
                if (id1 == id2)
                    return true;
        return false;
    }

    public static List<ItemStack> getPermutationsList(ItemStack stack) {
        List<ItemStack> permutations = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(stack))
            permutations.addAll(getPermutationsList(OreDictionary.getOreName(id)));
        if (permutations.size() == 0)
            permutations.add(stack);
        return stripWildcardDamage(permutations);
    }

    public static ItemStack[] getPermutations(ItemStack stack) {
        List<ItemStack> permutations = getPermutationsList(stack);
        return permutations.toArray(new ItemStack[permutations.size()]);
    }

    public static List<ItemStack> getPermutationsList(String oreName) {
        return stripWildcardDamage(OreDictionary.getOres(oreName));
    }

    public static ItemStack[] getPermutations(String oreName) {
        List<ItemStack> permutations = getPermutationsList(oreName);
        return permutations.toArray(new ItemStack[permutations.size()]);
    }

    public static String[] getOreNames(ItemStack stack) {
        List<String> names = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(stack))
            names.add(OreDictionary.getOreName(id));
        return names.toArray(new String[names.size()]);
    }

    private static List<ItemStack> stripWildcardDamage(List<ItemStack> list) {
        List<ItemStack> result = new ArrayList<>(list.size());
        for (ItemStack stack : list)
            result.add(new ItemStack(stack.getItem(), stack.getCount(), stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : stack.getItemDamage()));
        return result;
    }
}
