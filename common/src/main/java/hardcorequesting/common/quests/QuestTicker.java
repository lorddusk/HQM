package hardcorequesting.common.quests;

import net.minecraft.world.level.Level;

public class QuestTicker {
    
    private long hours;
    
    public QuestTicker() {}
    
    public void tick(Level level, boolean isClient) {
        if (level != null && level.getGameTime() / 1000 != hours) {
            hours = level.getGameTime() / 1000;
            if (!isClient) {
                for (Quest quest : Quest.getQuests().values()) {
                    int total = quest.getRepeatInfo().getTotalHours();
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
