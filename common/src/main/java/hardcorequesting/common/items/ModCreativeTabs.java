package hardcorequesting.common.items;

import dev.architectury.registry.CreativeTabRegistry;
import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {
    public static final CreativeTabRegistry.TabSupplier HQMTab = CreativeTabRegistry.create(
            new ResourceLocation(HardcoreQuestingCore.ID, "hardcorequesting"), // Tab ID
            () -> new ItemStack(ModItems.book.get()) // Icon
    );
}
