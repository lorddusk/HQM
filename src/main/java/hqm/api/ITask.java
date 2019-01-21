package hqm.api;

import hqm.api.page.IPage;
import hqm.api.reward.IReward;
import hqm.team.Team;
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
    
    @Nonnull
    IPage getRightSite();
    
    default void onRewardCollection(@Nonnull IReward reward, @Nonnull Team team){}
    
}
