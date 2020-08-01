package hardcorequesting.mixin;

import hardcorequesting.config.HQMConfig;
import hardcorequesting.items.QuestBookItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
    @Redirect(method = "dropAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean isEmpty(ItemStack stack) {
        return stack.isEmpty() || (stack.getItem() instanceof QuestBookItem && HQMConfig.getInstance().LOSE_QUEST_BOOK);
    }
}
