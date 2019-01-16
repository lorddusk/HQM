package hardcorequesting.api;

import hardcorequesting.api.render.ICustomIconRenderer;
import hardcorequesting.api.reward.IReward;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IQuest{
    
    void onCreation(IQuestline questline, UUID questId, NBTTagCompound additionalData, List<ITask> tasks, List<IHook> hooks, List<IReward> rewards);
    
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
    
    List<IReward> getRewards();
    
    int getX();
    
    int getY();
    
    /**
     * This method defines how many rewards can be gathered by completing the quest
     * or a invalid value if all rewards are given to the player.
     *
     * A value is valid if: 0 < value < rewards.size()
     *
     * @return The amount of rewards.
     */
    int getRewardAmount();
    
    @SideOnly(Side.CLIENT)
    @Nullable
    ICustomIconRenderer getIconRenderer();
    
}
