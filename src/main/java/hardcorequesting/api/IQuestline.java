package hardcorequesting.api;

import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.UUID;

public interface IQuestline{
    
    void onCreation(UUID questlineID, NBTTagCompound additionalData, List<IQuest> quests);
    
    UUID getUUID();
    
    String getNameTranslationKey();

    String getDescTranslationKey();
    
    int getSortIndex();
    
    List<IQuest> getQuests();
    
    NBTTagCompound getAdditionalData();
    
}
