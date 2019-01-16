package hardcorequesting.io;

import com.google.gson.annotations.SerializedName;
import hardcorequesting.api.*;
import hardcorequesting.api.reward.IReward;
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
    @SerializedName("class") private String className;
    @SerializedName("data") private NBTTagCompound data; // additional data
    @SerializedName("questlines") private List<QuestlineData> questlines;
    
    @Nullable
    public IQuestbook generateQuestline(boolean callQuestbookCreate, boolean callQuestlineCreate, boolean callQuestCreate, boolean callTaskCreate){
        if(this.className != null && !className.isEmpty()){
            IQuestbook questbook = HQMUtil.tryToCreateClassOfType(this.className, IQuestbook.class);
            if(questbook != null && this.questlines != null && callQuestbookCreate){
                questbook.onCreation(this.uuid, this.data, this.questlines.stream().map(questlineData -> questlineData.generateQuestline(questbook, callQuestlineCreate, callQuestCreate, callTaskCreate)).collect(Collectors.toList()));
            }
            return questbook;
        }
        return null;
    }
    
    public class QuestlineData {
    
        @SerializedName("uuid") private UUID uuid;
        @SerializedName("class") private String className;
        @SerializedName("quests") private List<QuestData> quests;
        @SerializedName("data") private NBTTagCompound data; // additional data
    
        @Nullable
        public IQuestline generateQuestline(IQuestbook questbook, boolean callQuestlineCreate, boolean callQuestCreate, boolean callTaskCreate){
            if(this.className != null && !className.isEmpty()){
                IQuestline questline = HQMUtil.tryToCreateClassOfType(this.className, IQuestline.class);
                if(questline != null && this.quests != null && callQuestlineCreate){
                    questline.onCreation(questbook, this.uuid, this.data, this.quests.stream().map(questData -> questData.generateQuest(questline, callQuestCreate, callTaskCreate)).collect(Collectors.toList()));
                }
                return questline;
            }
            return null;
        }
        
        public class QuestData {
    
            @SerializedName("uuid") private UUID uuid;
            @SerializedName("class") private String className;
            @SerializedName("tasks") private List<TaskData> tasks;
            @SerializedName("hooks") private List<HookData> hooks;
            @SerializedName("rewards") private List<RewardData> rewards;
            // name, desc, parent, x, y, icon
            @SerializedName("data") private NBTTagCompound data;
    
            @Nullable
            public IQuest generateQuest(IQuestline questline, boolean callQuestCreate, boolean callTaskCreate){
                if(this.className != null && !className.isEmpty()){
                    IQuest quest = HQMUtil.tryToCreateClassOfType(this.className, IQuest.class);
                    if(quest != null){
                        if(this.hooks != null && this.tasks != null && callQuestCreate){
                            List<ITask> tasks = this.tasks.stream().map(taskData -> taskData.generateTask(quest, callTaskCreate)).collect(Collectors.toList());
                            List<IHook> hooks = this.hooks.stream().map(hookData -> hookData.generateQuest(quest)).collect(Collectors.toList());
                            List<IReward> rewards = this.rewards.stream().map(rewardData -> rewardData.generateQuest(quest)).collect(Collectors.toList());
                            quest.onCreation(questline, this.uuid, this.data, tasks, hooks, rewards);
                        }
                    }
                    return quest;
                }
                return null;
            }
            
            public class HookData {
                @SerializedName("class") private String className;
                @SerializedName("data") private NBTTagCompound data;
    
                @Nullable
                public IHook generateQuest(IQuest quest){
                    if(this.className != null && !className.isEmpty()){
                        IHook hook = HQMUtil.tryToCreateClassOfType(this.className, IHook.class);
                        if(hook != null){
                            hook.onCreation(quest, this.data);
                        }
                        return hook;
                    }
                    return null;
                }
            }
    
            public class RewardData {
                @SerializedName("class") private String className;
                @SerializedName("data") private NBTTagCompound data;
        
                @Nullable
                public IReward generateQuest(IQuest quest){
                    if(this.className != null && !className.isEmpty()){
                        IReward reward = HQMUtil.tryToCreateClassOfType(this.className, IReward.class);
                        if(reward != null){
                            reward.onCreation(quest, this.data);
                        }
                        return reward;
                    }
                    return null;
                }
            }
            
            public class TaskData {
    
                @SerializedName("uuid") private UUID uuid;
                @SerializedName("class") private String className;
                // name, desc
                @SerializedName("data") private NBTTagCompound data;
                
                @Nullable
                public ITask generateTask(IQuest parent, boolean callCreate){
                    if(this.className != null && !className.isEmpty()){
                        ITask task = HQMUtil.tryToCreateClassOfType(this.className, ITask.class);
                        if(task != null && callCreate){
                            task.onCreation(parent, this.uuid, this.data);
                        }
                        return task;
                    }
                    return null;
                }
                
            }
        }
    }
}
