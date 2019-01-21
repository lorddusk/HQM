package hqm.api.reward;

import hqm.api.IQuest;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class DefaultReward implements IReward{
    
    private IQuest quest;
    
    @Override
    public void onCreation(@Nonnull IQuest quest, @Nonnull NBTTagCompound additionalData){
        this.quest = quest;
    }
    
    @Nonnull
    @Override
    public IQuest getQuest(){
        return this.quest;
    }
    
    @Nonnull
    @Override
    public NBTTagCompound getAdditionalData(){
        return new NBTTagCompound();
    }
    
}
