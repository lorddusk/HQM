package hardcorequesting.fabric.mixin;

import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.QuestBookItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Inventory.class)
public class MixinPlayerInventory {
    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean isEmpty(ItemStack stack) {
        return stack.isEmpty() || (stack.getItem() instanceof QuestBookItem && HQMConfig.getInstance().LOSE_QUEST_BOOK);
    }
}
