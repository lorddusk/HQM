package hardcorequesting.items;


import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ModItems {
	public static Item book;
    public static Item hearts;
    public static Item bags;
    public static Item invalidItem;

    public static void init() {
        book = new ItemQuestBook().setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BOOK_UNLOCALIZED_NAME);
        hearts = new ItemHeart();
        bags = new ItemBag();
        invalidItem = new ItemInvalid();

        GameRegistry.registerItem(book, ItemInfo.BOOK_UNLOCALIZED_NAME);
        GameRegistry.registerItem(hearts, ItemInfo.HEART_UNLOCALIZED_NAME);
        GameRegistry.registerItem(bags, ItemInfo.BAG_UNLOCALIZED_NAME);
        GameRegistry.registerItem(invalidItem,ItemInfo.INVALID_UNLOCALIZED_NAME);
	}
	

	
	public static void registerRecipes() {
		//Questing Book
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.book), Items.book, Items.string);

        //Hearts
        //1-1-1-1
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,3), new Object[]{
                new ItemStack(hearts,1,0),new ItemStack(hearts,1,0),new ItemStack(hearts,1,0),new ItemStack(hearts,1,0)
        });

        //1-1-2
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,3), new Object[]{
                new ItemStack(hearts,1,0),new ItemStack(hearts,1,0),new ItemStack(hearts,1,1)
        });

        //1-3
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,3), new Object[]{
                new ItemStack(hearts,1,0),new ItemStack(hearts,1,2)
        });

        //2-2
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,3), new Object[]{
                new ItemStack(hearts,1,1),new ItemStack(hearts,1,1)
        });

        //1-1
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,1), new Object[]{
                new ItemStack(hearts,1,0),new ItemStack(hearts,1,0)
        });

        //1-1-1
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,2), new Object[]{
                new ItemStack(hearts,1,0),new ItemStack(hearts,1,0),new ItemStack(hearts,1,0)
        });

        //1-2
        GameRegistry.addShapelessRecipe(new ItemStack(hearts,1,2), new Object[]{
                new ItemStack(hearts,1,0),new ItemStack(hearts,1,1)
        });



    }
}
