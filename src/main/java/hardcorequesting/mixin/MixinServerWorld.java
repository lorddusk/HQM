package hardcorequesting.mixin;

import hardcorequesting.event.WorldEventListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
    @Shadow @Final private WorldSaveHandler worldSaveHandler;
    
    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;save(Z)V"))
    private void save(ProgressListener progressListener, boolean flush, boolean bl, CallbackInfo ci) {
        WorldEventListener.onSave((ServerWorld) (Object) this);
    }
    
    @Inject(method = "init",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;getBiomeSource()Lnet/minecraft/world/biome/source/BiomeSource;"))
    private void init(LevelInfo levelInfo, CallbackInfo ci) {
        WorldEventListener.onCreate(worldSaveHandler, (ServerWorld) (Object) this);
    }
}
