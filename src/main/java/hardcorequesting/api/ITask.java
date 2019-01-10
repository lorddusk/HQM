package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

// here need to be a lot of additional calls, whenever something happens in mc
public interface ITask{
    
    void onCreation(UUID taskId, NBTTagCompound additionalData);
    
    @Nonnull
    UUID getUUID();
    
    @Nonnull
    String getNameTranslationKey();
    
    @Nullable
    String getDescTranslationKey();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
}
