package hardcorequesting.common.event;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.FileDataManager;
import hardcorequesting.common.quests.QuestLine;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldEventListener {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void onLoad(ResourceKey<Level> worldRegistryKey, ServerLevel world) {
        if (!world.isClientSide && worldRegistryKey.equals(Level.OVERWORLD)) {
            QuestLine questLine = QuestLine.reset();
            questLine.loadAll(HardcoreQuestingCore.packManager, FileDataManager.createForWorldData(world.getServer()));
        }
    }
    
    public static void onSave(ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            LOGGER.info("Saving HQM world data");
            QuestLine.getActiveQuestLine().saveData(FileDataManager.createForWorldData(world.getServer()));
        }
    }
}
