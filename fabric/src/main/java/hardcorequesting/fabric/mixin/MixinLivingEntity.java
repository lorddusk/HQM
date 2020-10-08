package hardcorequesting.fabric.mixin;

import hardcorequesting.fabric.HardcoreQuestingFabric;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(value = {LivingEntity.class, Player.class, ServerPlayer.class})
public class MixinLivingEntity {
    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        for (BiConsumer<LivingEntity, DamageSource> consumer : HardcoreQuestingFabric.LIVING_DEATH) {
            consumer.accept((LivingEntity) (Object) this, source);
        }
    }
}
