package hardcorequesting.fabric.mixin;

import hardcorequesting.fabric.HardcoreQuestingFabric;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(RunAroundLikeCrazyGoal.class)
public class MixinRunAroundLikeCrazyGoal {
    @Shadow @Final private AbstractHorse horse;
    
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;tameWithName(Lnet/minecraft/world/entity/player/Player;)Z"))
    private void bondWithPlayer(CallbackInfo ci) {
        for (BiConsumer<Player, Entity> consumer : HardcoreQuestingFabric.ANIMAL_TAME) {
            consumer.accept((Player) this.horse.getPassengers().get(0), horse);
        }
    }
}
