package hqm.api;

import net.minecraft.nbt.NBTTagCompound;

public interface ISimpleParsable{
    
    String getClassName();
    
    NBTTagCompound getData();
    
    void onLoad(NBTTagCompound nbt);
    
}
