package hqm.team;

import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class Team{
    
    @Nonnull private UUID teamID;
    @Nullable private NBTTagCompound data;
    @Nonnull private List<QuestCompletion> quests;
    
    public Team(@Nonnull UUID teamID, @Nullable NBTTagCompound data, @Nonnull List<QuestCompletion> quests){
        this.teamID = teamID;
        this.data = data;
        this.quests = quests;
    }
    
    @Nonnull
    public UUID getTeamID(){
        return teamID;
    }
    
    @Nonnull
    public NBTTagCompound getData(){
        return this.data != null ? this.data : (this.data = new NBTTagCompound());
    }
    
    @Nonnull
    public List<QuestCompletion> getQuests(){
        return quests;
    }
    
    public void writeTeamData(@Nonnull Consumer<NBTTagCompound> consumer){
        consumer.accept(this.getData());
    }
    
    public void writeQuestData(@Nonnull UUID questID, @Nonnull Consumer<NBTTagCompound> consumer){
        this.getQuests().stream().filter(questCompletion -> questCompletion.getQuestID().equals(questID))
            .findFirst()
            .ifPresent(questCompletion -> questCompletion.writeQuestData(consumer));
    }
    
    public void writeTaskData(@Nonnull UUID questID, @Nonnull UUID taskID, @Nonnull Consumer<NBTTagCompound> consumer){
        this.getQuests().stream().filter(questCompletion -> questCompletion.getQuestID().equals(questID))
            .findFirst()
            .ifPresent(questCompletion -> questCompletion.writeTaskData(taskID, consumer));
    }
    
    public static class QuestCompletion {
        @Nonnull private UUID questID;
        @Nullable private NBTTagCompound questData;
        @Nonnull private List<TaskCompletion> tasks;
    
        public QuestCompletion(@Nonnull UUID questID, @Nullable NBTTagCompound questData, @Nonnull List<TaskCompletion> tasks){
            this.questID = questID;
            this.questData = questData;
            this.tasks = tasks;
        }
    
        @Nonnull
        public UUID getQuestID(){
            return questID;
        }
    
        @Nonnull
        public NBTTagCompound getQuestData(){
            return this.questData != null ? this.questData : (this.questData = new NBTTagCompound());
        }
    
        @Nonnull
        public List<TaskCompletion> getTasks(){
            return tasks;
        }
    
        public void writeQuestData(@Nonnull Consumer<NBTTagCompound> consumer){
            consumer.accept(this.getQuestData());
        }
    
        public void writeTaskData(@Nonnull UUID taskID, @Nonnull Consumer<NBTTagCompound> consumer){
            this.getTasks().stream().filter(taskCompletion -> taskCompletion.getTaskID().equals(taskID))
                .findFirst()
                .ifPresent(taskCompletion -> taskCompletion.writeTaskData(consumer));
        }
    
        public static class TaskCompletion {
            @Nonnull private UUID taskID;
            @Nullable private NBTTagCompound taskData;
    
            public TaskCompletion(@Nonnull UUID taskID, @Nullable NBTTagCompound taskData){
                this.taskID = taskID;
                this.taskData = taskData;
            }
    
            @Nonnull
            public UUID getTaskID(){
                return taskID;
            }
    
            @Nonnull
            public NBTTagCompound getTaskData(){
                return this.taskData != null ? this.taskData : (this.taskData = new NBTTagCompound());
            }
            
            public void writeTaskData(@Nonnull Consumer<NBTTagCompound> consumer){
                consumer.accept(this.getTaskData());
            }
        }
    }
    
}
