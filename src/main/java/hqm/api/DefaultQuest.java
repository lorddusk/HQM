package hqm.api;

import hqm.client.EmptyIconRenderer;
import hqm.api.render.ICustomIconRenderer;
import hqm.api.reward.IReward;
import hardcorequesting.util.HQMUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DefaultQuest implements IQuest{
    
    private IQuestline questline;
    private UUID questId = UUID.randomUUID();
    @Nullable private UUID parentId;
    @Nullable private String nameTranslationKey, descTranslationKey;
    private List<ITask> tasks = new ArrayList<>();
    private List<IHook> hooks = new ArrayList<>();
    private List<IReward> rewards = new ArrayList<>();
    private int posX, posY, rewardAmount;
    @Nullable private ICustomIconRenderer renderer;
    
    @Override
    public void onCreation(@Nonnull IQuestline questline, @Nonnull UUID questId, @Nonnull NBTTagCompound additionalData, @Nonnull List<ITask> tasks, @Nonnull List<IHook> hooks, @Nonnull List<IReward> rewards){
        this.questline = questline;
        this.questId = questId;
        this.parentId = additionalData.getUniqueId("Parent");
        this.nameTranslationKey = additionalData.getString("Name");
        this.descTranslationKey = additionalData.getString("Desc");
        this.tasks = tasks;
        this.hooks = hooks;
        this.rewards = rewards;
        this.posX = additionalData.getInteger("X");
        this.posY = additionalData.getInteger("Y");
        this.rewardAmount = additionalData.getInteger("RewardAmount");
        this.renderer = HQMUtil.getInstanceFromNBT(additionalData, "Renderer", ICustomIconRenderer.class);
    }
    
    @Nonnull
    @Override
    public IQuestline getQuestline(){
        return this.questline;
    }
    
    @Nonnull
    @Override
    public UUID getUUID(){
        return this.questId;
    }
    
    @Nullable
    @Override
    public UUID getParentUUID(){
        return this.parentId;
    }
    
    @Nullable
    @Override
    public String getNameTranslationKey(){
        return this.nameTranslationKey;
    }
    
    @Nullable
    @Override
    public String getDescTranslationKey(){
        return this.descTranslationKey;
    }
    
    @Nonnull
    @Override
    public NBTTagCompound getAdditionalData(){
        NBTTagCompound data = new NBTTagCompound();
        if(this.getParentUUID() != null){
            data.setUniqueId("Parent", this.getParentUUID());
        }
        if(this.getNameTranslationKey() != null){
            data.setString("Name", this.getNameTranslationKey());
        }
        if(this.getDescTranslationKey() != null){
            data.setString("Desc", this.getDescTranslationKey());
        }
        data.setInteger("X", this.getX());
        data.setInteger("Y", this.getY());
        data.setInteger("RewardAmount", this.getRewardAmount());
        if(this.renderer != null){
            HQMUtil.setInstanceToNBT(data, "Renderer", this.renderer);
        }
        return data;
    }
    
    @Nonnull
    @Override
    public List<ITask> getTasks(){
        return this.tasks;
    }
    
    @Nonnull
    @Override
    public List<IHook> getHooks(){
        return this.hooks;
    }
    
    @Nonnull
    @Override
    public List<IReward> getRewards(){
        return rewards;
    }
    
    @Override
    public int getX(){
        return this.posX;
    }
    
    @Override
    public int getY(){
        return this.posY;
    }
    
    @Override
    public int getRewardAmount(){
        return this.rewardAmount;
    }
    
    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public ICustomIconRenderer getIconRenderer(){
        return this.renderer != null ? this.renderer : EmptyIconRenderer.get();
    }
    
}
