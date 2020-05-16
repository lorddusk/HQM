package hardcorequesting.client.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ClientSound extends PositionedSoundInstance {
    
    public ClientSound(Identifier resource, float volume, float pitch) {
        super(resource, SoundCategory.BLOCKS, volume, pitch, false, 0, AttenuationType.NONE, 0, 0, 0, false);
    }
}
