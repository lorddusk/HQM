package hardcorequesting.io;

import com.google.gson.annotations.SerializedName;
import hardcorequesting.api.team.Party;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamData{
    
    @SerializedName("uuid") private UUID uuid;
    @SerializedName("data") private NBTTagCompound additionalData;
    @SerializedName("quests") private List<QuestCompletionData> questData;
    
    @Nullable
    public Party toTeam(){
        return this.uuid != null && this.additionalData != null && this.questData != null ?
            new Party(this.uuid, this.additionalData, this.questData.stream()
                                                                    .map(QuestCompletionData::toQuestCompletion)
                                                                    .filter(Objects::nonNull)
                                                                    .collect(Collectors.toList())) : null;
    }
    
    @Nonnull
    public static TeamData fromTeam(@Nonnull Party party){
        TeamData td = new TeamData();
        td.uuid = party.getTeamID();
        td.additionalData = party.getData();
        td.questData = new ArrayList<>();
        td.questData.addAll(party.getQuests().stream().map(QuestCompletionData::fromTaskCompletion).collect(Collectors.toList()));
        return td;
    }
    
    public static class QuestCompletionData {
        @SerializedName("uuid") private UUID questId;
        @SerializedName("data") private NBTTagCompound additionalData;
        @SerializedName("tasks") private List<TaskCompletionData> tasks;
    
        @Nullable
        private Party.QuestCompletion toQuestCompletion(){
            return this.questId != null && this.additionalData != null && this.tasks != null ?
                new Party.QuestCompletion(this.questId, this.additionalData, this.tasks.stream()
                                                                                       .map(TaskCompletionData::toTaskCompletion)
                                                                                       .filter(Objects::nonNull)
                                                                                       .collect(Collectors.toList())) : null;
        }
    
        @Nonnull
        private static QuestCompletionData fromTaskCompletion(@Nonnull Party.QuestCompletion qc){
            QuestCompletionData qcd = new QuestCompletionData();
            qcd.questId = qc.getQuestID();
            qcd.additionalData = qc.getQuestData();
            qcd.tasks = new ArrayList<>();
            qcd.tasks.addAll(qc.getTasks().stream().map(TaskCompletionData::fromTaskCompletion).collect(Collectors.toList()));
            return qcd;
        }
        
        public static class TaskCompletionData{
            @SerializedName("uuid") private UUID taskUuid;
            @SerializedName("data") private NBTTagCompound additionalData;
            
            @Nullable
            private Party.QuestCompletion.TaskCompletion toTaskCompletion(){
                return this.taskUuid != null && this.additionalData != null ? new Party.QuestCompletion.TaskCompletion(this.taskUuid, this.additionalData) : null;
            }
            
            @Nonnull
            private static TaskCompletionData fromTaskCompletion(@Nonnull Party.QuestCompletion.TaskCompletion tc){
                TaskCompletionData tcd = new TaskCompletionData();
                tcd.taskUuid = tc.getTaskID();
                tcd.additionalData = tc.getTaskData();
                return tcd;
            }
        }
    }
}
