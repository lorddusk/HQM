package hardcorequesting.quests;


import net.minecraft.entity.player.EntityPlayer;

public class RepeatInfo {
    private int days;
    private int hours;
    private RepeatType type;

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

    public RepeatType getType() {
        return type;
    }

    public String getMessage(Quest quest, EntityPlayer player) {
        return type.getMessage(quest, player, days, hours);
    }

    public String getShortMessage() {
        return type.getShortMessage(days, hours);
    }
}
