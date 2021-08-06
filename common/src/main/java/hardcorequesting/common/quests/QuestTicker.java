package hardcorequesting.common.quests;

import hardcorequesting.common.HardcoreQuestingCore;
import net.minecraft.world.level.Level;

public class QuestTicker {
    
    private long hours;
    
    public QuestTicker(boolean isClient) {
        if (isClient) {
            HardcoreQuestingCore.platform.registerOnClientTick(minecraftClient -> tick(minecraftClient.level, true));
        } else
            HardcoreQuestingCore.platform.registerOnServerTick(minecraftServer -> tick(minecraftServer.overworld(), false));
    }
    
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
