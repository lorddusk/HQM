package hardcorequesting.common.items.crafting;

import hardcorequesting.common.items.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class BookCatalystRecipe extends ShapedRecipe {
    
    public BookCatalystRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(id, group, width, height, ingredients, result);
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(container);
        
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(ModItems.book.get()) || stack.is(ModItems.enabledBook.get())) {
                remaining.set(i, stack);
            }
        }
        return remaining;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.bookCatalystSerializer.get();
    }
}
