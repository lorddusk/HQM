package hardcorequesting.fabric;

import com.google.gson.JsonObject;
import hardcorequesting.common.items.crafting.BookCatalystRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Same as the corresponding class in the forge module, but does not extend ForgeRegistryEntry
 */
public class BookCatalystRecipeSerializer implements RecipeSerializer<BookCatalystRecipe> {
    @Override
    public BookCatalystRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json);
    
        return new BookCatalystRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getResultItem());
    }
    
    @Override
    public BookCatalystRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer);
    
        return new BookCatalystRecipe(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getResultItem());
    }
    
    @Override
    public void toNetwork(FriendlyByteBuf buffer, BookCatalystRecipe recipe) {
        RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
    }
}
