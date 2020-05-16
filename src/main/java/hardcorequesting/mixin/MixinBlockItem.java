package hardcorequesting.mixin;

import hardcorequesting.event.EventTrigger;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class MixinBlockItem {
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE",
                                            target = "Lnet/minecraft/item/BlockItem;place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
                                            shift = At.Shift.AFTER))
    private void placeBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        EventTrigger.instance().onBlockPlaced((BlockItem) (Object) this, context);
    }
}
