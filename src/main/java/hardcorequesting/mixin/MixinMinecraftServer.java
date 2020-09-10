package hardcorequesting.mixin;

import hardcorequesting.event.WorldEventListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Inject(at = @At(value = "HEAD"), method = "setInitialSpawn", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void setupSpawn(ServerLevel serverWorld, ServerLevelData serverWorldProperties, boolean bl, boolean bl2, boolean bl3, CallbackInfo ci) {
//        WorldEventListener.onCreate(serverWorld);
    }
}