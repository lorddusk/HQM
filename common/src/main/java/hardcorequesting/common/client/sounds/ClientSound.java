package hardcorequesting.common.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public class ClientSound extends SimpleSoundInstance {

    public ClientSound(ResourceLocation resourceLocation, float volume, float pitch, RandomSource randomSource) {
        super(resourceLocation, SoundSource.BLOCKS, volume, pitch, randomSource, false, 0, Attenuation.NONE, 0, 0, 0, false);
    }
}
