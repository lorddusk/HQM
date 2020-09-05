package hardcorequesting.items;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {
    public static CreativeModeTab HQMTab = FabricItemGroupBuilder.create(new ResourceLocation("hardcorequesting", "hardcorequesting"))
            .icon(() -> new ItemStack(ModItems.book))
            .build();
}
