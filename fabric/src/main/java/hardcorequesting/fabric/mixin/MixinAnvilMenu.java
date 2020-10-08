package hardcorequesting.fabric.mixin;

import hardcorequesting.fabric.HardcoreQuestingFabric;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu extends ItemCombinerMenu {
    public MixinAnvilMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        super(menuType, i, inventory, containerLevelAccess);
    }
    
    @Inject(method = "onTake", at = @At(value = "HEAD"))
    private void postCraft(Player player, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        for (BiConsumer<Player, ItemStack> consumer : HardcoreQuestingFabric.CRAFTING) {
            consumer.accept(player, itemStack);
        }
    }
}
