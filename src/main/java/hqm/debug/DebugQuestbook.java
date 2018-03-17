package hqm.debug;

import com.google.common.collect.Lists;
import hqm.HQM;
import hqm.quest.QuestLine;
import hqm.quest.Questbook;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class DebugQuestbook extends Questbook{

    private static final List<String> desc = Lists.newArrayList("This is the internal debug questbook.",
            "It is designed to test all the various features that are already implemented or planned.");

    private static final List<String> tooltip = Lists.newArrayList();

    public DebugQuestbook() {
        super("Debug", new UUID(0L, 0L), desc, tooltip, new ResourceLocation(HQM.MODID, "textures/gui/front.png"), new ArrayList<>());
        QuestLine numberOne = new DebugQuestLine(0, Collections.emptyList());
        this.addQuestLine(numberOne);
        QuestLine numberTwo = new DebugQuestLine(1, Collections.emptyList());
        this.addQuestLine(numberTwo);
    }

}
