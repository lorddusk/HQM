package hardcorequesting.items;


import hardcorequesting.RegisterHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ModItems {
    public static Item book = new ItemQuestBook().setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BOOK_UNLOCALIZED_NAME);
    public static Item hearts = new ItemHeart().setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.HEART_UNLOCALIZED_NAME);
    public static Item bags = new ItemBag().setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BAG_UNLOCALIZED_NAME);
    public static Item invalidItem = new ItemInvalid().setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.INVALID_UNLOCALIZED_NAME);

    public static void init() {
        RegisterHelper.registerItem(book);
        RegisterHelper.registerItem(hearts);
        RegisterHelper.registerItem(bags);
        RegisterHelper.registerItem(invalidItem);
    }

    public static void initRender(){
        RegisterHelper.registerItemRenderer(book);
        RegisterHelper.registerItemRenderer(hearts);
        RegisterHelper.registerItemRenderer(bags);
        RegisterHelper.registerItemRenderer(invalidItem);
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
