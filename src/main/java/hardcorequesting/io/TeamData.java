package hardcorequesting.io;

import com.google.common.collect.Table;
import com.google.gson.annotations.SerializedName;
import hardcorequesting.api.IQuestbook;
import hardcorequesting.api.IQuestline;
import hardcorequesting.api.team.ITeam;
import hardcorequesting.util.HQMUtil;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamData{
    
    @SerializedName("class") private String className;
    @SerializedName("uuid") private UUID uuid;
    @SerializedName("quests") private List<QuestCompletionData> questData;
    @SerializedName("data") private NBTTagCompound additionalData;
    
    @Nullable
    public ITeam generateTeam(){
        if(this.className != null && !className.isEmpty()){
            ITeam team = HQMUtil.tryToCreateClassOfType(this.className, ITeam.class);
            if(team != null && this.questData != null){
            
            
            }
            return team;
        }
        return null;
    }
    
    public class QuestCompletionData {
        @SerializedName("uuid") private UUID questId;
        @SerializedName("player") private UUID completer;
        @SerializedName("data") private NBTTagCompound additionalData;
        @SerializedName("tasks") private List<TaskCompletionData> tasks;
    
        public class TaskCompletionData{
            @SerializedName("uuid") private UUID taskUuid;
            @SerializedName("player") private UUID completer;
            @SerializedName("data") private NBTTagCompound additionalData;
        }
    }
}
