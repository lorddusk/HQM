package hardcorequesting.forge;

import com.google.gson.JsonObject;
import hardcorequesting.common.items.crafting.BookCatalystRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Same as the corresponding class in the fabric module, but extends ForgeRegistryEntry
 */
public class BookCatalystRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<BookCatalystRecipe> {
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
