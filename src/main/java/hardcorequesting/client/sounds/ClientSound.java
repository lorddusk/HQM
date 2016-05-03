package hardcorequesting.client.sounds;

import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class ClientSound extends PositionedSound {
    public ClientSound(ResourceLocation resource, float volume, float pitch) {
        super(resource, SoundCategory.BLOCKS);
        this.volume = volume;
        this.pitch = pitch;
        this.xPosF = 0.0F;
        this.yPosF = 0.0F;
        this.zPosF = 0.0F;
        this.repeat = false;
        this.repeatDelay = 0;
        this.attenuationType = AttenuationType.NONE;
    }
}
