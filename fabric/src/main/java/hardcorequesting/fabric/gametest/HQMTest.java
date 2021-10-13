package hardcorequesting.fabric.gametest;

import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestSet;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class HQMTest extends HQMTestBase implements FabricGameTest {
    
    private static final UUID QUEST_1 = UUID.fromString("570a381c-2bfe-11ec-8d3d-0242ac130003");
    
    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        setupTestQuestData();
        FabricGameTest.super.invokeTestMethod(context, method);
    }
    
    /**
     * Initializes quest data used for testing
     */
    private void setupTestQuestData() {
        clearData();
    
        QuestSet testSet = createQuestSet("Test 1");
        createQuest("Test quest", QUEST_1, testSet);
    }
    
    @GameTest(template = EMPTY_STRUCTURE)
    public void questLineExists(GameTestHelper helper) {
        
        if (QuestLine.getActiveQuestLine() == null)
            helper.fail("Quest line has not been initialized.");
        else helper.succeed();
    }
    
    @GameTest(template = EMPTY_STRUCTURE)
    public void initAsIncomplete(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        assertQuestCompletionStatus(QUEST_1, false, player);
        helper.succeed();
    }
}
