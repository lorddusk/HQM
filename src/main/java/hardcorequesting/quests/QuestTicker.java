package hardcorequesting.quests;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;

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
                for (Quest quest : Quest.getQuests()) {
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

    public void save(DataWriter dw) {
        dw.writeData(ticks, DataBitHelper.TICKS);
        dw.writeData(hours, DataBitHelper.HOURS);
    }

    public void load(DataReader dr) {
        ticks = dr.readData(DataBitHelper.TICKS);
        hours = dr.readData(DataBitHelper.HOURS);
    }
}
