package hardcorequesting.mixin;

import hardcorequesting.event.EventTrigger;
import hardcorequesting.event.PlayerDeathEventListener;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {PlayerEntity.class, ServerPlayerEntity.class})
public class MixinPlayerEntity {
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        PlayerDeathEventListener.instance.onLivingDeath((PlayerEntity) (Object) this, source);
        EventTrigger.instance().onLivingDeath((PlayerEntity) (Object) this, source);
    }
}
