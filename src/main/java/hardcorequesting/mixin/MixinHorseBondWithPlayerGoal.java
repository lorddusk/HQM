package hardcorequesting.mixin;

import hardcorequesting.event.EventTrigger;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseBondWithPlayerGoal.class)
public class MixinHorseBondWithPlayerGoal {
    @Shadow @Final private HorseBaseEntity horse;
    
    @Inject(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/HorseBaseEntity;bondWithPlayer(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private void bondWithPlayer(CallbackInfo ci) {
        EventTrigger.instance().onAnimalTame((PlayerEntity) this.horse.getPassengerList().get(0), horse);
    }
}
