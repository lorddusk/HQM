package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public interface ITask{
    
    void onCreation(UUID taskId, NBTTagCompound additionalData);
    
    UUID getUUID();
    
    String getNameTranslationKey();
    
    String getDescritpionTranslationKey();
    
    NBTTagCompound getAdditionalData();
    
}
