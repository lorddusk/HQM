package hqm.api.page;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRenderPart{
    
    void onCreation(int x, int y, int width, int height);
    
    int getX();
    
    int getY();
    
    int getWidth();
    
    int getHeight();
    
    @SideOnly(Side.CLIENT)
    void render();
    
}
