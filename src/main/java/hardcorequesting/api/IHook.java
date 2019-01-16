package hardcorequesting.api;

import hardcorequesting.api.reward.IReward;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.List;

public interface IHook{
    
    void onCreation(@Nonnull IQuest quest, @Nonnull NBTTagCompound additionalData);
    
    @Nonnull
    IQuest getQuest();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
    default void onTaskCompletion(@Nonnull ITask task, @Nonnull EntityPlayer player){}
    
    default void onQuestCompletion(@Nonnull IQuest quest, @Nonnull EntityPlayer player){}
    
    default void onQuestRewardsClaimed(@Nonnull IQuest quest, @Nonnull List<IReward> rewards, @Nonnull EntityPlayer player){}
    
}
