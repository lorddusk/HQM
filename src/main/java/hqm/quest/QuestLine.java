package hqm.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author canitzp
 */
public class QuestLine {

    private final String name;
    private final int index;
    private final List<String> description;
    private final List<Quest> quests;

    public QuestLine(String name, int index, List<String> description, List<Quest> quests) {
        this.name = name;
        this.index = index;
        this.description = description;
        this.quests = quests;
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
        return this.quests.stream().filter(quest -> quest.parentId == null || team.hasParentSolved(quest)).collect(Collectors.toList());
    }

    public List<Quest> getCompleted(Team team){
        return this.quests.stream().filter(team::hasSolved).collect(Collectors.toList());
    }

    public List<Quest> getUnlockedUncompleted(Team team){
        return getUnlocked(team).stream().filter(quest -> quest.parentId == null || !team.hasSolved(quest)).collect(Collectors.toList());
    }

}
