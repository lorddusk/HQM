package hardcorequesting.fabric.mixin;

import hardcorequesting.fabric.HardcoreQuestingFabric;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(FurnaceResultSlot.class)
public class MixinFurnaceResultSlot extends Slot {
    @Shadow @Final private Player player;
    
    public MixinFurnaceResultSlot(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }
    
    @Inject(method = "checkTakeAchievements", at = @At(value = "INVOKE",
                                                       target = "Lnet/minecraft/world/item/ItemStack;onCraftedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;I)V",
                                                       shift = At.Shift.AFTER))
    private void postCraft(ItemStack stack, CallbackInfo ci) {
        for (BiConsumer<Player, ItemStack> consumer : HardcoreQuestingFabric.CRAFTING) {
            consumer.accept(this.player, stack);
        }
    }
}
