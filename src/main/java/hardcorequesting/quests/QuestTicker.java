package hardcorequesting.quests;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class QuestTicker {

    private int hours;
    private int ticks;

    public QuestTicker(boolean isClient) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        tick(false);
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        tick(true);
    }


    public void tick(boolean isClient) {
        if (++ticks == 1000) {
            ticks = 0;
            hours++;
            if (!isClient) {
                for (Quest quest : Quest.getQuests().values()) {
                    int total = quest.getRepeatInfo().getDays() * 24 + quest.getRepeatInfo().getHours();
                    if (quest.getRepeatInfo().getType() == RepeatType.INTERVAL) {
                        if (total != 0 && hours % total == 0) {
                            quest.resetAll();
                        }
                    } else if (quest.getRepeatInfo().getType() == RepeatType.TIME) {
                        quest.resetOnTime(hours - total);
                    }
                }
            }
        }
    }


    public int getHours() {
        return hours;
    }
}
