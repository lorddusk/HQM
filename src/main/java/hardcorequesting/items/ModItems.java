package hardcorequesting.items;


import hardcorequesting.crafting.Recipes;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModItems {

    public static ItemQuestBook book = new ItemQuestBook();
    public static ItemBag bags = new ItemBag();
    public static ItemInvalid invalidItem = new ItemInvalid();

    public static ItemHeart quarterheart = new ItemHeart(0);
    public static ItemHeart halfheart = new ItemHeart(1);
    public static ItemHeart threequartsheart = new ItemHeart(2);
    public static ItemHeart heart = new ItemHeart(3);
    public static ItemHeart rottenheart = new ItemHeart(4);
    
    public static void init() {
        RegisterHelper.registerItem(book);
        RegisterHelper.registerItem(bags);
        RegisterHelper.registerItem(invalidItem);
        
        RegisterHelper.registerItem(quarterheart);
        RegisterHelper.registerItem(halfheart);
        RegisterHelper.registerItem(threequartsheart);
        RegisterHelper.registerItem(heart);
        RegisterHelper.registerItem(rottenheart);
    }

    public static void registerRecipes() {
        //Questing Book
        Recipes.addShapelessRecipe(new ItemStack(ModItems.book), Items.BOOK, Items.STRING);

        //Hearts
        //1-1-1-1
        Recipes.addShapelessRecipe(new ItemStack(heart, 1), quarterheart, quarterheart, quarterheart, quarterheart);

        //1-1-2
        Recipes.addShapelessRecipe(new ItemStack(heart, 1), quarterheart, quarterheart, halfheart);

        //1-3
        Recipes.addShapelessRecipe(new ItemStack(heart, 1), quarterheart, threequartsheart);

        //2-2
        Recipes.addShapelessRecipe(new ItemStack(heart, 1), halfheart, halfheart);

        //1-1
        Recipes.addShapelessRecipe(new ItemStack(halfheart, 1), quarterheart, quarterheart);

        //1-1-1
        Recipes.addShapelessRecipe(new ItemStack(threequartsheart, 1), quarterheart, quarterheart, quarterheart);

        //1-2
        Recipes.addShapelessRecipe(new ItemStack(threequartsheart, 1), quarterheart, halfheart);
    }
}
