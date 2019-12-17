package hqm.quest;

import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class Team {

    public String name;
    public int color, lives;
    public List<UUID> teamMembers;

    public List<UUID> currentlyFinishedTasks = new ArrayList<>();
    public List<UUID> finishedQuests = new ArrayList<>();

    public Team(String name, int color, int lives, List<UUID> teamMembers) {
        this.name = name;
        this.color = color;
        this.lives = lives;
        this.teamMembers = teamMembers;
    }

    public Team setLists(List<UUID> tasks, List<UUID> quests){
        this.currentlyFinishedTasks = tasks;
        this.finishedQuests = quests;
        return this;
    }

    public Team finishTask(Quest quest, ITask task){
        ITask nextTask = quest.getNextTask(task);
        if(nextTask != null){
            this.currentlyFinishedTasks.add(task.getId());
        } else {
            this.currentlyFinishedTasks.clear();
            this.finishedQuests.add(quest.id);
        }
        return this;
    }

    public boolean hasSolved(Quest quest){
        return this.finishedQuests.contains(quest.id);
    }

    public boolean hasParentSolved(Quest quest){
        return this.finishedQuests.contains(quest.parent);
    }

    public boolean containsPlayer(PlayerEntity player){
        return this.teamMembers.contains(player.getUniqueID());
    }

    public UUID getAdmin(){
        return this.teamMembers.size() > 0 ? this.teamMembers.get(0) : null;
    }

}
