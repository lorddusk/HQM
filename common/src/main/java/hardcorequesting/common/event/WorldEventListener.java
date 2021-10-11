package hardcorequesting.common.event;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.FileDataManager;
import hardcorequesting.common.quests.QuestLine;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class WorldEventListener {
    public static void onLoad(ResourceKey<Level> worldRegistryKey, ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine questLine = QuestLine.reset();
            questLine.loadAll(HardcoreQuestingCore.packManager, FileDataManager.createForWorldData(world.getServer()));
        }
    }
    
    public static void onSave(ServerLevel world) {
        if (!world.isClientSide && world.dimension().equals(Level.OVERWORLD)) {
            QuestLine.getActiveQuestLine().save(null, FileDataManager.createForWorldData(world.getServer()));
        }
    }
}
