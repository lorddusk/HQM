package hardcorequesting.mixin;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.event.WorldEventListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V", ordinal = 0),
            method = "run")
    private void afterSetupServer(CallbackInfo info) {
        HardcoreQuesting.instance.serverStarting((MinecraftServer) (Object) this);
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0,
                     shift = At.Shift.AFTER), method = "createWorlds", locals = LocalCapture.CAPTURE_FAILHARD)
    private void loadOverWorld(WorldSaveHandler worldSaveHandler, LevelProperties properties, LevelInfo levelInfo, WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci, ServerWorld world) {
        WorldEventListener.onLoad(worldSaveHandler, world);
    }
}