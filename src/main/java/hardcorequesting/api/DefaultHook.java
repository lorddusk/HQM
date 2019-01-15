package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class DefaultHook implements IHook{
    
    private IQuest quest;
    
    @Override
    public void onCreation(IQuest quest, NBTTagCompound additionalData){
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
