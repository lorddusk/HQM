package hardcorequesting.items;


import cpw.mods.fml.common.registry.GameRegistry;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModItems {
    public static ItemQuestBook book = new ItemQuestBook();
    public static ItemHeart hearts = new ItemHeart();
    public static ItemBag bags = new ItemBag();
    public static ItemInvalid invalidItem = new ItemInvalid();

    public static void init() {
        RegisterHelper.registerItem(book);
        RegisterHelper.registerItem(hearts);
        RegisterHelper.registerItem(bags);
        RegisterHelper.registerItem(invalidItem);
    }

    public static void registerRecipes() {
        //Questing Book
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.book), Items.book, Items.string);

        //Hearts
        //1-1-1-1
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 3), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0));

        //1-1-2
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 3), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 1));

        //1-3
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 3), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 2));

        //2-2
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 3), new ItemStack(hearts, 1, 1), new ItemStack(hearts, 1, 1));

        //1-1
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 1), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0));

        //1-1-1
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 2), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 0));

        //1-2
        GameRegistry.addShapelessRecipe(new ItemStack(hearts, 1, 2), new ItemStack(hearts, 1, 0), new ItemStack(hearts, 1, 1));
    }
}
