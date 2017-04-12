package hardcorequesting.config;

import hardcorequesting.ModInformation;
import net.minecraftforge.common.config.Config;

/**
 * @author canitzp
 */
//TODO switch to the new annotation based config reading
//@Config(modid = ModInformation.ID, name = "hqm/hqmconfig")
public class HQMConfig {

    @Config.Name("MaxLives")
    @Config.Comment("Use this to set the maximum lives obtainable (Max 255)")
    public static int MAX_LIVES = 20;

}
