package hardcorequesting.mixin;

import hardcorequesting.event.EventTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {
    @Shadow public ServerWorld world;
    
    @Shadow public ServerPlayerEntity player;
    
    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE",
                                               target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
    private void onBreak(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        BlockState blockState = this.world.getBlockState(blockPos);
        EventTrigger.instance().onBlockBreak(blockPos, blockState, player);
    }
}
