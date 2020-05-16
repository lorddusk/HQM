package hardcorequesting.mixin;

import hardcorequesting.event.EventTrigger;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class MixinPlayerAdvancementTracker {
    @Shadow private ServerPlayerEntity owner;
    
    @Inject(method = "grantCriterion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                     shift = At.Shift.AFTER))
    private void reward(Advancement advancement, String criterion, CallbackInfoReturnable<Boolean> cir) {
        EventTrigger.instance().onAdvancement(owner);
    }
}
