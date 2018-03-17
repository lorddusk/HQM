package hqm.debug;

import hqm.quest.Quest;
import hqm.quest.QuestLine;

import java.util.Collections;
import java.util.List;

/**
 * @author canitzp
 */
public class DebugQuestLine extends QuestLine {

    public DebugQuestLine(int index, List<Quest> quests) {
        super("Debug" + index, index, Collections.singletonList("This is debug questline number " + index), quests);
    }

}
