package hqm.quest;

import java.util.List;

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

}
