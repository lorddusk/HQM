package hqm.api;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface IQuestline{
    
    void onCreation(IQuestbook questbook, UUID questlineID, NBTTagCompound additionalData, List<IQuest> quests);
    
    @Nonnull
    IQuestbook getQuestbook();
    
    UUID getUUID();
    
    String getNameTranslationKey();

    String getDescTranslationKey();
    
    int getSortIndex();
    
    List<IQuest> getQuests();
    
    NBTTagCompound getAdditionalData();
    
}
