package hqm.api;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public abstract class DefaultQuestline implements IQuestline{
    
    private IQuestbook questbook;
    private UUID questlineId;
    private String nameTranslationKey, descTranslationKey;
    private int sortIndex;
    private List<IQuest> quests;
    
    @Override
    public void onCreation(IQuestbook questbook, UUID questlineID, NBTTagCompound additionalData, List<IQuest> quests){
        this.questbook = questbook;
        this.questlineId = questlineID;
        this.nameTranslationKey = additionalData.getString("Name");
        this.descTranslationKey = additionalData.getString("Desc");
        this.sortIndex = additionalData.getInteger("Sort");
        this.quests = quests;
    }
    
    @Nonnull
    @Override
    public IQuestbook getQuestbook(){
        return this.questbook;
    }
    
    @Override
    public UUID getUUID(){
        return this.questlineId;
    }
    
    @Override
    public String getNameTranslationKey(){
        return this.nameTranslationKey;
    }
    
    @Override
    public String getDescTranslationKey(){
        return this.descTranslationKey;
    }
    
    @Override
    public int getSortIndex(){
        return this.sortIndex;
    }
    
    @Override
    public List<IQuest> getQuests(){
        return this.quests;
    }
    
    @Override
    public NBTTagCompound getAdditionalData(){
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("Sort", this.getSortIndex());
        data.setString("Name", this.getNameTranslationKey());
        if(this.getDescTranslationKey() != null){
            data.setString("Desc", this.getDescTranslationKey());
        }
        return data;
    }
}
