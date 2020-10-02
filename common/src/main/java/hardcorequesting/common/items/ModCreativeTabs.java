package hardcorequesting.common.items;

import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {
    public static CreativeModeTab HQMTab = HardcoreQuestingCore.platform.createTab(new ResourceLocation("hardcorequesting", "hardcorequesting"), () -> new ItemStack(ModItems.book));
}
