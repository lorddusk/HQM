package hardcorequesting.quests;


import hardcorequesting.FileVersion;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import net.minecraft.entity.player.EntityPlayer;

public class RepeatInfo {
    private int days;
    private int hours;
    private RepeatType type;

    public RepeatInfo(RepeatType type, int days, int hours) {
        int total = Math.min(DataBitHelper.HOURS.getMaximum(), days * 24 + hours);

        this.days = total / 24;
        this.hours = hours % 24;

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

    public void load(DataReader dr, FileVersion version) {
        if (version.contains(FileVersion.REPEATABLE_QUESTS)) {
            type = RepeatType.values()[dr.readData(DataBitHelper.REPEAT_TYPE)];
            if (type.isUseTime()) {
                int total = dr.readData(DataBitHelper.HOURS);
                days = total / 24;
                hours = total % 24;
            }
        } else {
            type = RepeatType.NONE;
        }
    }

    public void save(DataWriter dw) {
        dw.writeData(type.ordinal(), DataBitHelper.REPEAT_TYPE);
        if (type.isUseTime()) {
            int total = days * 24 + hours;
            dw.writeData(total, DataBitHelper.HOURS);
        }
    }
}
