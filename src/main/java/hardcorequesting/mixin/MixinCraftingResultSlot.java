package hardcorequesting.mixin;

import hardcorequesting.event.EventTrigger;
import net.minecraft.container.CraftingResultSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class MixinCraftingResultSlot {
    @Shadow @Final private PlayerEntity player;
    
    @Shadow @Final private CraftingInventory craftingInv;
    
    @Inject(method = "onCrafted(Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE",
                                                                            target = "Lnet/minecraft/item/ItemStack;onCraft(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;I)V",
                                                                            shift = At.Shift.AFTER))
    private void postCraft(ItemStack stack, CallbackInfo ci) {
        EventTrigger.instance().onCrafting(this.player, stack, this.craftingInv);
    }
}
