package hqm.api.render;

import hqm.api.ISimpleParsable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ICustomIconRenderer extends ISimpleParsable{
    
    @SideOnly(Side.CLIENT)
    void render();
    
}
