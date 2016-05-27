package hardcorequesting.client.interfaces;

import hardcorequesting.ModInformation;
import hardcorequesting.config.ModConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

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
