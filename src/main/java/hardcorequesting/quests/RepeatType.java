package hardcorequesting.quests;

import hardcorequesting.client.interfaces.GuiColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

public enum RepeatType {
    NONE("none", false) {
        @Environment(EnvType.CLIENT)
        @Override
        public String getMessage(Quest quest, Player player, int days, int hours) {
            return null;
        }
    },
    INSTANT("instant", false) {
        @Environment(EnvType.CLIENT)
        @Override
        public String getMessage(Quest quest, Player player, int days, int hours) {
            return super.getMessage(quest, player, days, hours) + GuiColor.GRAY + I18n.get("hqm.repeat.instant.message");
        }
    
        @Environment(EnvType.CLIENT)
        @Override
        public String getShortMessage(int days, int hours) {
            return GuiColor.YELLOW + I18n.get("hqm.repeat.instant.message");
        }
    },
    INTERVAL("interval", true) {
        @Environment(EnvType.CLIENT)
        @Override
        public String getMessage(Quest quest, Player player, int days, int hours) {
            return super.getMessage(quest, player, days, hours) + GuiColor.GRAY + I18n.get("hqm.repeat.interval.message") + "\n" + formatTime(days, hours) + "\n" + formatResetTime(quest, player, days, hours);
        }
    
        @Environment(EnvType.CLIENT)
        @Override
        public String getShortMessage(int days, int hours) {
            return GuiColor.YELLOW + I18n.get("hqm.repeat.interval.message") + " (" + days + ":" + hours + ")";
        }
    },
    TIME("time", true) {
        @Environment(EnvType.CLIENT)
        @Override
        public String getMessage(Quest quest, Player player, int days, int hours) {
            return super.getMessage(quest, player, days, hours) + GuiColor.GRAY + I18n.get("hqm.repeat.time.message") + "\n" + formatTime(days, hours) + formatRemainingTime(quest, player, days, hours);
        }
    
        @Environment(EnvType.CLIENT)
        @Override
        public String getShortMessage(int days, int hours) {
            return GuiColor.YELLOW + I18n.get("hqm.repeat.time.message") + " (" + days + ":" + hours + ")";
        }
    };
    
    private String id;
    private boolean useTime;
    
    RepeatType(String id, boolean useTime) {
        this.id = id;
        this.useTime = useTime;
    }
    
    @Environment(EnvType.CLIENT)
    private static String formatRemainingTime(Quest quest, Player player, int days, int hours) {
        if (!quest.getQuestData(player).available) {
            int total = days * 24 + hours;
            int time = quest.getQuestData(player).time;
            int current = Quest.clientTicker.getHours();
            
            total = time + total - current;
            
            return "\n" + formatResetTime(quest, player, total / 24, total % 24);
        } else {
            return "";
        }
    }
    
    @Environment(EnvType.CLIENT)
    private static String formatResetTime(Quest quest, Player player, int days, int hours) {
        if (days == 0 && hours == 0) {
            return GuiColor.RED + I18n.get("hqm.repeat.invalid");
        }
        
        int total = days * 24 + hours;
        int resetHoursTotal = total - Quest.clientTicker.getHours() % total;
        
        int resetDays = resetHoursTotal / 24;
        int resetHours = resetHoursTotal % 24;
        
        if (!quest.isAvailable(player)) {
            return GuiColor.YELLOW + I18n.get("hqm.repeat.resetIn", formatTime(resetDays, resetHours));
        } else {
            return GuiColor.GRAY + I18n.get("hqm.repeat.nextReset", formatTime(resetDays, resetHours));
        }
    }
    
    @Environment(EnvType.CLIENT)
    private static String formatTime(int days, int hours) {
        String str = GuiColor.GRAY.toString();
        if (days > 0) {
            str += GuiColor.LIGHT_GRAY;
        }
        str += days;
        str += " ";
        str += I18n.get("hqm.repeat." + (days == 1 ? "day" : "days"));
        
        str += GuiColor.GRAY;
        
        str += " " + I18n.get("hqm.repeat.and") + " ";
        
        if (hours > 0) {
            str += GuiColor.LIGHT_GRAY;
        }
        
        str += hours;
        str += " ";
        str += I18n.get("hqm.repeat." + (hours == 1 ? "hour" : "hours"));
        
        return str;
    }
    
    @Environment(EnvType.CLIENT)
    public String getName() {
        return I18n.get("hqm.repeat." + id + ".title");
    }
    
    @Environment(EnvType.CLIENT)
    public String getDescription() {
        return I18n.get("hqm.repeat." + id + ".desc");
    }
    
    public boolean isUseTime() {
        return useTime;
    }
    
    @Environment(EnvType.CLIENT)
    public String getMessage(Quest quest, Player player, int days, int hours) {
        return GuiColor.YELLOW + I18n.get("hqm.repeat.repeatable") + "\n";
    }
    
    @Environment(EnvType.CLIENT)
    public String getShortMessage(int days, int hours) {
        return null;
    }
}
