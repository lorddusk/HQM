package hardcorequesting.api;

import hardcorequesting.api.reward.IReward;
import hardcorequesting.api.team.Party;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class DefaultTask implements ITask{
    
    private IQuest quest;
    private UUID taskId;
    private String nameTranslationKey, descTranslationKey;
    
    @Override
    public void onCreation(IQuest quest, UUID taskId, NBTTagCompound additionalData){
        this.taskId = taskId;
        this.nameTranslationKey = additionalData.getString("Name");
        this.descTranslationKey = additionalData.getString("Desc");
    }
    
    @Nonnull
    @Override
    public IQuest getQuest(){
        return this.quest;
    }
    
    @Nonnull
    @Override
    public UUID getUUID(){
        return this.taskId;
    }
    
    @Nonnull
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
        data.setString("Name", this.getNameTranslationKey());
        if(this.getDescTranslationKey() != null){
            data.setString("Desc", this.getDescTranslationKey());
        }
        return data;
    }
    
}
