package hardcorequesting.client.interfaces;

import net.minecraftforge.fml.client.config.GuiConfig;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.ModInformation;
import hardcorequesting.config.ModConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;

import java.util.List;

public class HQMConfigGui extends GuiConfig {
    public HQMConfigGui(GuiScreen parentScreen) {
        super(parentScreen,
                ModConfig.getConfigElements(),
                ModInformation.ID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ModConfig.config.toString()));
    }
}
