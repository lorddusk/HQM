package hardcorequesting.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import hardcorequesting.common.items.crafting.BookCatalystRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Optional;

/**
 * Same as the corresponding class in the forge module, but does not extend ForgeRegistryEntry
 */
public class BookCatalystRecipeSerializer implements RecipeSerializer<BookCatalystRecipe> {
    
    @Override
    public BookCatalystRecipe fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(friendlyByteBuf);
        ShapedRecipePattern pattern = new ShapedRecipePattern(recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), Optional.empty());
        return new BookCatalystRecipe(recipe.getGroup(), pattern, recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public Codec<BookCatalystRecipe> codec() {
        return RecipeSerializer.SHAPED_RECIPE.codec().flatXmap(recipe -> {
            ShapedRecipePattern pattern = new ShapedRecipePattern(recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), Optional.empty());
            BookCatalystRecipe bookCatalystRecipe = new BookCatalystRecipe(recipe.getGroup(), pattern, recipe.getResultItem(RegistryAccess.EMPTY));
            return DataResult.success(bookCatalystRecipe);
        }, bookCatalystRecipe -> {
            throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
        });
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, BookCatalystRecipe recipe) {
        RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
    }
}
