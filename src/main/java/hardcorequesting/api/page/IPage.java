package hardcorequesting.api.page;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPage{
    
    void onCreation(ISite site);
    
    ISite getSite();
    
    //todo
    @SideOnly(Side.CLIENT)
    void render();
    
}
