package hardcorequesting.integration.rei;

import hardcorequesting.items.ModItems;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.util.Identifier;

public class REIIntegration implements REIPluginV0 {
    @Override
    public Identifier getPluginIdentifier() {
        return new Identifier("hardcorequesting:rei_integration");
    }
    
    @Override
    public void registerEntries(EntryRegistry entryRegistry) {
        entryRegistry.getStacksList().removeIf(stack -> stack.getItem() != null && stack.getItem() == ModItems.enabledBook);
    }
}
