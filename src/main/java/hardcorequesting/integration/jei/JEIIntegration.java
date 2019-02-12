package hardcorequesting.integration.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

@JEIPlugin
public class JEIIntegration implements IModPlugin {
    
    private static IJeiRuntime runtime = null;
    private static Boolean enabled = null;
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    public static void showItemStack(@Nonnull ItemStack stack) {
        if(!stack.isEmpty()){
            IRecipeRegistry register = runtime.getRecipeRegistry();
    
            IFocus<?> focus = register.createFocus(IFocus.Mode.OUTPUT, stack);
            runtime.getRecipesGui().show(focus);
        }
    }
}
