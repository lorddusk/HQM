package hardcorequesting.common.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import hardcorequesting.common.items.crafting.BookCatalystRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.Optional;
import java.util.function.Function;

public class BookCatalystRecipeSerializer implements RecipeSerializer<BookCatalystRecipe> {
    private static final MapCodec<BookCatalystRecipe> CODEC = ShapedRecipe.Serializer.CODEC.flatXmap(recipe -> {
        ShapedRecipePattern pattern = new ShapedRecipePattern(recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), Optional.empty());
        BookCatalystRecipe bookCatalystRecipe = new BookCatalystRecipe(recipe.getGroup(), pattern, recipe.getResultItem(RegistryAccess.EMPTY));
        return DataResult.success(bookCatalystRecipe);
    }, bookCatalystRecipe -> DataResult.error(() -> "Serializing ShapedRecipe is not implemented yet."));

    private static final StreamCodec<RegistryFriendlyByteBuf, BookCatalystRecipe> STREAM_CODEC = ShapedRecipe.Serializer.STREAM_CODEC.map(recipe -> {
        ShapedRecipePattern pattern = new ShapedRecipePattern(recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), Optional.empty());
        return new BookCatalystRecipe(recipe.getGroup(), pattern, recipe.getResultItem(RegistryAccess.EMPTY));
    }, Function.identity());

    @Override
    public MapCodec<BookCatalystRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BookCatalystRecipe> streamCodec() {

        return STREAM_CODEC;
    }
}
