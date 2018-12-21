package hardcorequesting.integration.jei;

import net.minecraft.item.ItemStack;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IFocus;
import net.minecraftforge.fml.common.Loader;

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
