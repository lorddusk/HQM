package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IQuest{
    
    void onCreation(IQuestline questline, UUID questId, NBTTagCompound additionalData, List<ITask> tasks, List<IHook> hooks);
    
    @Nonnull
    IQuestline getQuestline();
    
    @Nonnull
    UUID getUUID();
    
    @Nullable
    UUID getParentUUID();
    
    @Nonnull
    String getNameTranslationKey();
    
    @Nullable
    String getDescTranslationKey();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
    List<ITask> getTasks();
    
    List<IHook> getHooks();
    
    int getX();
    
    int getY();
    
    // todo add parameter
    @SideOnly(Side.CLIENT)
    void renderIcon();
    
}
