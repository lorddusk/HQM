package hardcorequesting.common.quests;

import hardcorequesting.common.HardcoreQuestingCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.Level;

public class QuestTicker {
    
    private long hours;
    
    @Environment(EnvType.CLIENT)
    public static QuestTicker initClientTicker() {
        QuestTicker ticker = new QuestTicker();
        HardcoreQuestingCore.platform.registerOnClientTick(minecraftClient -> ticker.tick(minecraftClient.level, true));
        return ticker;
    }
    
    public static QuestTicker initServerTicker() {
        QuestTicker ticker = new QuestTicker();
        HardcoreQuestingCore.platform.registerOnServerTick(minecraftServer -> ticker.tick(minecraftServer.overworld(), false));
        return ticker;
    }
    
    private QuestTicker() {}
    
    public void tick(Level level, boolean isClient) {
        if (level != null && level.getGameTime() / 1000 != hours) {
            hours = level.getGameTime() / 1000;
            if (!isClient) {
                for (Quest quest : Quest.getQuests().values()) {
                    int total = quest.getRepeatInfo().getDays() * 24 + quest.getRepeatInfo().getHours();
                    if (quest.getRepeatInfo().getType() == RepeatType.INTERVAL) {
                        if (total != 0 && hours % total == 0) {
                            quest.resetAll();
                        }
                    } else if (quest.getRepeatInfo().getType() == RepeatType.TIME) {
                        quest.resetOnTime(hours - total);
                    } else if (quest.getRepeatInfo().getType() == RepeatType.INSTANT) {
                        quest.resetAll();
                    }
                }
            }
        }
    }
    
    public long getHours() {
        return hours;
    }
}
