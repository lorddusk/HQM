package hardcorequesting.fabric.mixin;

import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse {
    
    // Architectury only fires the animal tame event for TamableAnimal.tame, but horses are tamed through this method
    @Inject(method = "tameWithName", at = @At("RETURN"))
    private void afterTame(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            EntityEvent.ANIMAL_TAME.invoker().tame((AbstractHorse) (Object) this, player);
        }
    }
}
