package hardcorequesting.fabric.mixin;

import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.event.PlayerDeathEventListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Player.class, ServerPlayer.class})
public class MixinPlayerEntity {
    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        PlayerDeathEventListener.instance.onLivingDeath((Player) (Object) this, source);
        EventTrigger.instance().onLivingDeath((Player) (Object) this, source);
    }
}
