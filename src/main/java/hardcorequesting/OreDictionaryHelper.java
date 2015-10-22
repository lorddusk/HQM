package hardcorequesting;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class OreDictionaryHelper
{
    public static List<ItemStack> getPermutationsList(ItemStack itemStack)
    {
        List<ItemStack> permutations = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(itemStack))
            permutations.addAll(getPermutationsList(OreDictionary.getOreName(id)));
        if (permutations.size() == 0)
            permutations.add(itemStack);
        return permutations;
    }

    public static ItemStack[] getPermutations(ItemStack itemStack)
    {
        List<ItemStack> permutations = getPermutationsList(itemStack);
        return permutations.toArray(new ItemStack[permutations.size()]);
    }

    public static List<ItemStack> getPermutationsList(String oreName)
    {
        return OreDictionary.getOres(oreName);
    }

    public static ItemStack[] getPermutations(String oreName)
    {
        List<ItemStack> permutations = getPermutationsList(oreName);
        return permutations.toArray(new ItemStack[permutations.size()]);
    }

    public static String[] getOreNames(ItemStack itemStack)
    {
        List<String> names = new ArrayList<>();
        for (int id : OreDictionary.getOreIDs(itemStack))
            names.add(OreDictionary.getOreName(id));
        return names.toArray(new String[names.size()]);
    }
}
