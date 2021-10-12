package hardcorequesting.fabric;

import hardcorequesting.common.io.LocalDataManager;
import hardcorequesting.common.quests.QuestLine;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.lang.reflect.Method;

public class HQMTest implements FabricGameTest {
    
    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        setupTestQuestData();
        FabricGameTest.super.invokeTestMethod(context, method);
    }
    
    /**
     * Initializes quest data used for testing
     */
    private void setupTestQuestData() {
        // Initialize data with an empty data manager
        LocalDataManager dataManager = new LocalDataManager();
        QuestLine.reset().loadAll(dataManager, dataManager);
        
    }
    
    @GameTest(template = EMPTY_STRUCTURE)
    public void questLineExists(GameTestHelper helper) {
        
        if (QuestLine.getActiveQuestLine() == null)
            helper.fail("Quest line has not been initialized.");
        else helper.succeed();
    }
}
