package hardcorequesting.client.interfaces;

import hardcorequesting.ModInformation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.DefaultGuiFactory;

public class HQMModGuiFactory extends DefaultGuiFactory {

    public HQMModGuiFactory() {
        super(ModInformation.ID, ModInformation.NAME);
        //ConfigManager.sync(this.modid, Config.Type.INSTANCE);
        //System.out.println(HQMConfig.MAX_LIVES);
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen)
    {  
        return new HQMConfigGui(parentScreen);
    }
}
