package hardcorequesting.api;

import hardcorequesting.api.reward.IReward;
import hardcorequesting.api.team.ITeam;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

// here need to be a lot of additional calls, whenever something happens in mc
public interface ITask{
    
    void onCreation(IQuest quest, UUID taskId, NBTTagCompound additionalData);
    
    @Nonnull
    IQuest getQuest();
    
    @Nonnull
    UUID getUUID();
    
    @Nonnull
    String getNameTranslationKey();
    
    @Nullable
    String getDescTranslationKey();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
    void onRewardCollection(IReward reward, ITeam team);
    
}
