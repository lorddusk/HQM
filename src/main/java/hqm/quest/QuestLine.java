package hqm.quest;

import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author canitzp
 */
public class QuestLine {

    private final String name;
    private final int index;
    private NBTTagCompound data;
    private final List<String> description;
    private final List<Quest> quests;

    public QuestLine(String name, int index, List<String> description, List<Quest> quests) {
        this.name = name;
        this.index = index;
        this.description = description;
        this.quests = quests;
    }

    public QuestLine setData(NBTTagCompound data) {
        this.data = data;
        return this;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public List<Quest> getUnlocked(Team team){
        return this.quests.stream().filter(quest -> quest.isOpen(this, team)).collect(Collectors.toList());
    }

    public List<Quest> getCompleted(Team team){
        return this.quests.stream().filter(quest -> quest.isDone(team)).collect(Collectors.toList());
    }

    public List<Quest> getUnlockedUncompleted(Team team){
        return getUnlocked(team).stream().filter(quest -> !quest.isDone(team)).collect(Collectors.toList());
    }

    public Quest getQuest(UUID id){
        for(Quest quest : this.quests){
            if(quest.id.equals(id)){
                return quest;
            }
        }
        return null;
    }

}
