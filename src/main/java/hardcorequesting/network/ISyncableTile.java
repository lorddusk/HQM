package hardcorequesting.network;

import net.minecraft.nbt.NBTTagCompound;

public interface ISyncableTile{
    
    NBTTagCompound getSyncData();
    
    void onData(NBTTagCompound data);
    
}
