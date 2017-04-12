package hardcorequesting.client.interfaces;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.ModInformation;
import hardcorequesting.config.HQMConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.DefaultGuiFactory;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class HQMModGuiFactory extends DefaultGuiFactory {

    public HQMModGuiFactory() {
        super(ModInformation.ID, ModInformation.NAME);
        //ConfigManager.sync(this.modid, Config.Type.INSTANCE);
        //System.out.println(HQMConfig.MAX_LIVES);
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return HQMConfigGui.class;
    }

    //TODO remove this freaking work around
    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        throw new AbstractMethodError();
    }
}
