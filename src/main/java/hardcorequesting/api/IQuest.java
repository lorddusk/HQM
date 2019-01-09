package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IQuest{
    
    void onCreation(UUID questId, NBTTagCompound additionalData, List<ITask> tasks);
    
    @Nonnull
    UUID getUUID();
    
    @Nullable
    UUID getParentUUID();
    
    @Nonnull
    String getNameTranslationKey();
    
    @Nullable
    String getDescritpionTranslationKey();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
    List<ITask> getTasks();
    
    int getX();
    
    int getY();
    
    // todo add parameter
    @SideOnly(Side.CLIENT)
    void renderIcon();
    
}
