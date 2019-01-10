package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.UUID;

public class DefaultQuestline implements IQuestline{
    
    private UUID questlineId;
    private String nameTranslationKey, descTranslationKey;
    private int sortIndex;
    private List<IQuest> quests;
    
    @Override
    public void onCreation(UUID questlineID, NBTTagCompound additionalData, List<IQuest> quests){
        this.questlineId = questlineID;
        this.nameTranslationKey = additionalData.getString("Name");
        this.descTranslationKey = additionalData.getString("Desc");
        this.sortIndex = additionalData.getInteger("Sort");
        this.quests = quests;
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
