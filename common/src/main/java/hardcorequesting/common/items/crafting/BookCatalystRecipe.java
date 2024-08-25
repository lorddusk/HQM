package hardcorequesting.common.items.crafting;

import hardcorequesting.common.items.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class BookCatalystRecipe extends ShapedRecipe {
    
    public BookCatalystRecipe(String group, ShapedRecipePattern shapedRecipePattern, ItemStack result) {
        super(group, CraftingBookCategory.MISC, shapedRecipePattern, result);
    }
    
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = super.getRemainingItems(container);
        
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.is(ModItems.book.get()) || stack.is(ModItems.enabledBook.get())) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.bookCatalystSerializer.get();
    }
}
