package hardcorequesting.common.quests;


import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RepeatInfo {
    
    private final int days;
    private final int hours;
    private final RepeatType type;
    
    public RepeatInfo(RepeatType type, int days, int hours) {
        this.days = days;
        this.hours = hours;
        this.type = type;
    }
    
    public int getDays() {
        return days;
    }
    
    public int getHours() {
        return hours;
    }
    
    public int getTotalHours() {
        return days * 24 + hours;
    }
    
    public RepeatType getType() {
        return type;
    }
    
    public List<FormattedText> getMessage(Quest quest, Player player) {
        return type.getMessage(quest, player, days, hours);
    }
    
    public List<FormattedText> getShortMessage() {
        return type.getShortMessage(days, hours);
    }
}
