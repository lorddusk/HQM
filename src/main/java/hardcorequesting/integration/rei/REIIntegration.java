package hardcorequesting.integration.rei;

import hardcorequesting.items.ModItems;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.resources.ResourceLocation;

public class REIIntegration implements REIPluginV0 {
    @Override
    public ResourceLocation getPluginIdentifier() {
        return new ResourceLocation("hardcorequesting:rei_integration");
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        entryRegistry.getStacksList().removeIf(stack -> stack.getItem() != null && stack.getItem() == ModItems.enabledBook);
    }
}
