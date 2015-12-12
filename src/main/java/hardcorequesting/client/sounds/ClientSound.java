package hardcorequesting.client.sounds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class ClientSound extends PositionedSound {
    public ClientSound(ResourceLocation resource, float volume, float pitch) {
        super(resource);
        this.volume = volume;
        this.field_147663_c = pitch;
        this.xPosF = 0.0F;
        this.yPosF = 0.0F;
        this.zPosF = 0.0F;
        this.repeat = false;
        this.field_147665_h = 0;
        this.field_147666_i = AttenuationType.NONE;
    }
}
