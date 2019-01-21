package hardcorequesting.api;

import hardcorequesting.api.page.IPage;
import hardcorequesting.api.reward.IReward;
import hardcorequesting.api.team.Party;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    
    default void onRewardCollection(@Nonnull IReward reward, @Nonnull Party team){}
    
}
