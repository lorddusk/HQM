package hardcorequesting.integration.jei;

public class JEIIntegration {}
/*
@JEIPlugin
public class JEIIntegration implements IModPlugin {
    
    private static IJeiRuntime runtime = null;
    private static Boolean enabled = null;
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
    
    public static void showItemStack(@NotNull ItemStack stack) {
        if (!stack.isEmpty()) {
            IRecipeRegistry register = runtime.getRecipeRegistry();
            
            IFocus<?> focus = register.createFocus(IFocus.Mode.OUTPUT, stack);
            runtime.getRecipesGui().show(focus);
        }
    }
}

 */
