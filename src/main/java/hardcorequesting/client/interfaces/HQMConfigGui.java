package hardcorequesting.client.interfaces;

import cpw.mods.fml.client.config.GuiConfig;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.ModInformation;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;

import java.util.List;

/**
 * Created by Tim on 6/26/2014.
 */
public class HQMConfigGui extends GuiConfig {
    public HQMConfigGui(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(HardcoreQuesting.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), ModInformation.ID, true, false, GuiConfig.getAbridgedConfigPath(HardcoreQuesting.config.toString()));
    }
}
