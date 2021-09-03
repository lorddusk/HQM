package hardcorequesting.common.items.crafting;

import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class ModRecipes {
    public static Supplier<RecipeSerializer<?>> bookCatalystSerializer;
    
    public static void init() {
        bookCatalystSerializer = HardcoreQuestingCore.platform.registerBookRecipeSerializer("book_catalyst_shaped");
    }
}
