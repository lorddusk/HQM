package hardcorequesting.io;

import com.google.gson.annotations.SerializedName;
import hardcorequesting.api.IQuest;
import hardcorequesting.api.IQuestline;
import hardcorequesting.api.ITask;
import hardcorequesting.util.HQMUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class represents the save format 3 of HQM.
 * Format 2 questbooks should be convertable to format 3.
 * Most of the thinking about the structure was made while
 * creating the format for the intended, but never done, rewrite.
 *
 * Every data type consists on the basics, that are needed, but
 * everyone has a {@link NBTTagCompound} called data, to specify
 * other things. This also makes it future proof.
 *
 * All strings that are rendered are written as translationKey, so
 * we could add a language page, to specify the keys and their values
 * and then multiple languages are possible within the questbook.
 *
 * All {@link UUID} should be unique withing all Questbooks within one game,
 * but they haven't to. Only the {@link QuestbookData#uuid} isn't allow
 * to collide with any other existing questbook!
 * The other uuids shouldn't collide with other ones from the same category
 * withing the book.
 *
 *
 * @since 09.01.2019 - HQM 6(?)
 * @author canitzp
 */
public class QuestbookData{
    
    @SerializedName("uuid") private UUID uuid;
    @SerializedName("name") private String nameTranslationKey;
    @SerializedName("icon") private String iconPath; // can be a ResourceLocation, but also a absolute path (starts with anything but '/') on the file system or a path relative to the questbook.json folder ('/' means questbook.json folder)
    @SerializedName("desc") private String descriptionTranslationKey;
    @SerializedName("tooltip") private String tooltipAdditionTranslationKey;
    @SerializedName("dim") private List<Integer> allowedDimensionIds;
    @SerializedName("data") private NBTTagCompound data; // additional data
    @SerializedName("questlines") private List<QuestlineData> questlines;
    
    public class QuestlineData {
    
        @SerializedName("uuid") private UUID uuid;
        @SerializedName("class") private String className;
        @SerializedName("quests") private List<QuestData> quests;
        @SerializedName("data") private NBTTagCompound data; // additional data
    
        @Nullable
        public IQuestline generateQuestline(boolean callQuestlineCreate, boolean callQuestCreate, boolean callTaskCreate){
            if(this.className != null && !className.isEmpty()){
                IQuestline questline = HQMUtil.tryToCreateClassOfType(this.className, IQuestline.class);
                if(questline != null && this.quests != null && callQuestlineCreate){
                    questline.onCreation(this.uuid, this.data, this.quests.stream().map(questData -> questData.generateQuest(callQuestCreate, callTaskCreate)).collect(Collectors.toList()));
                }
                return questline;
            }
            return null;
        }
        
        public class QuestData {
    
            @SerializedName("uuid") private UUID uuid;
            @SerializedName("class") private String className;
            @SerializedName("tasks") private List<TaskData> tasks;
            // name, desc, parent, x, y, icon
            @SerializedName("data") private NBTTagCompound data;
    
            @Nullable
            public IQuest generateQuest(boolean callQuestCreate, boolean callTaskCreate){
                if(this.className != null && !className.isEmpty()){
                    IQuest quest = HQMUtil.tryToCreateClassOfType(this.className, IQuest.class);
                    if(quest != null && this.tasks != null && callQuestCreate){
                        quest.onCreation(this.uuid, this.data, this.tasks.stream().map(taskData -> taskData.generateTask(callTaskCreate)).collect(Collectors.toList()));
                    }
                    return quest;
                }
                return null;
            }
            
            public class TaskData {
    
                @SerializedName("uuid") private UUID uuid;
                @SerializedName("class") private String className;
                // name, desc
                @SerializedName("data") private NBTTagCompound data;
                
                @Nullable
                public ITask generateTask(boolean callCreate){
                    if(this.className != null && !className.isEmpty()){
                        ITask task = HQMUtil.tryToCreateClassOfType(this.className, ITask.class);
                        if(task != null && callCreate){
                            task.onCreation(this.uuid, this.data);
                        }
                        return task;
                    }
                    return null;
                }
                
            }
        }
    }
}
