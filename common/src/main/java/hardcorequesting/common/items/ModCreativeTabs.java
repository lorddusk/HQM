package hardcorequesting.common.items;

import dev.architectury.registry.CreativeTabRegistry;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.util.RegisterHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {
    public static final CreativeModeTab HQMTab = CreativeTabRegistry.create(builder -> {
                builder.title(Component.translatable("itemGroup.hqm"));
                builder.icon(() -> new ItemStack(ModItems.book.get()));
                builder.displayItems((itemDisplayParameters, output) -> {
                    for (Item item : BuiltInRegistries.ITEM) {
                        if(item.arch$registryName().getNamespace().equals(HardcoreQuestingCore.ID)){
                            output.accept(item);
                        }
                    }
                });
            }
    );

    public static void init(){
        HardcoreQuestingCore.platform.registerTab("tab", ()-> HQMTab);
    }
}
