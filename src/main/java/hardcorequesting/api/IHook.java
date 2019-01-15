package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public interface IHook{
    
    void onCreation(IQuest quest, NBTTagCompound additionalData);
    
    @Nonnull
    IQuest getQuest();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
}
