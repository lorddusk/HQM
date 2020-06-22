package hardcorequesting.mixin;

import hardcorequesting.event.WorldEventListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Inject(at = @At(value = "HEAD"), method = "setupSpawn", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void setupSpawn(ServerWorld serverWorld, ServerWorldProperties serverWorldProperties, boolean bl, boolean bl2, boolean bl3, CallbackInfo ci) {
        WorldEventListener.onCreate(serverWorld);
    }
}