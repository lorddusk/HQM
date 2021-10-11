package hardcorequesting.fabric;

import hardcorequesting.common.quests.QuestLine;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class HQMTest {
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questLineExists(GameTestHelper helper) {
        
        if (QuestLine.getActiveQuestLine() == null)
            helper.fail("Quest line has not been initialized.");
        else helper.succeed();
    }
}
