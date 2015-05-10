package hardcorequesting.quests;


import hardcorequesting.client.interfaces.GuiColor;
import net.minecraft.entity.player.EntityPlayer;

public enum  RepeatType {
    NONE("Not repeatable", "This quest is not repeatable and can therefore only be completed once.", false) {
        @Override
        public String getMessage(Quest quest, EntityPlayer player, int days, int hours) {
            return null;
        }
    },
    INSTANT("Instant repeat", "As soon as this quest is completed it can be completed again for another set of rewards", false) {
        @Override
        public String getMessage(Quest quest, EntityPlayer player, int days, int hours) {
            return super.getMessage(quest, player, days, hours) + GuiColor.GRAY + "Instant Cooldown";
        }

        @Override
        public String getShortMessage(int days, int hours) {
            return GuiColor.YELLOW + "Instant Cooldown";
        }
    },
    INTERVAL("Interval repeat", "At a specific interval this quest will be reset and available for completion again. The quest is only reset if it has already been completed.", true) {
        @Override
        public String getMessage(Quest quest, EntityPlayer player, int days, int hours) {
            return super.getMessage(quest, player, days, hours) + GuiColor.GRAY + "Refreshes on interval\n" + formatTime(days, hours) + "\n" + formatResetTime(quest, player, days, hours);
        }

        @Override
        public String getShortMessage(int days, int hours) {
            return GuiColor.YELLOW + "Refreshes on interval (" + days + ":" + hours + ")";
        }
    },
    TIME("Cooldown repeat", "After completing this quest it goes on a cooldown, when this cooldown ends you can complete the quest again.", true) {
        @Override
        public String getMessage(Quest quest, EntityPlayer player, int days, int hours) {
            return super.getMessage(quest, player, days, hours) + GuiColor.GRAY + "Cooldown on completion\n" + formatTime(days, hours) + formatRemainingTime(quest, player, days, hours);
        }

        @Override
        public String getShortMessage(int days, int hours) {
            return GuiColor.YELLOW + "Cooldown on completion (" + days + ":" + hours + ")";
        }
    };

    private static String formatRemainingTime(Quest quest, EntityPlayer player, int days, int hours) {
        if (!quest.getQuestData(player).available) {
            int total = days * 24 + hours;
            int time = quest.getQuestData(player).time;
            int current = Quest.clientTicker.getHours();

            total = time + total - current;

            return "\n" + formatResetTime(quest, player, total / 24, total % 24);
        }else{
            return "";
        }
    }

    private static String formatResetTime(Quest quest, EntityPlayer player, int days, int hours) {
        if (days == 0 && hours == 0) {
            return GuiColor.RED + "Invalid time";
        }

        int total = days * 24 + hours;
        int resetHoursTotal = total - Quest.clientTicker.getHours() % total;

        int resetDays = resetHoursTotal / 24;
        int resetHours = resetHoursTotal % 24;

        if (!quest.isAvailable(player)) {
            return GuiColor.YELLOW + "Resets in " + formatTime(resetDays, resetHours);
        }else{
            return GuiColor.GRAY + "Next reset: " + formatTime(resetDays, resetHours);
        }
    }

    private static String formatTime(int days, int hours) {
        String str = GuiColor.GRAY.toString();
        if (days > 0) {
            str += GuiColor.LIGHT_GRAY;
        }
        str += days;
        str += " ";
        str += days == 1 ? "day" : "days";

        str += GuiColor.GRAY;

        str += " and ";

        if (hours > 0) {
            str += GuiColor.LIGHT_GRAY;
        }

        str += hours;
        str += " ";
        str += hours == 1 ? "hour" : "hours";

        return str;
    }

    RepeatType(String name, String description, boolean useTime) {
        this.name = name;
        this.description = description;
        this.useTime = useTime;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUseTime() {
        return useTime;
    }

    private String name;
    private String description;
    private boolean useTime;


    public String getMessage(Quest quest, EntityPlayer player, int days, int hours) {
        return GuiColor.YELLOW + "Repeatable Quest\n";
    }

    public String getShortMessage(int days, int hours) {
        return null;
    }
}
