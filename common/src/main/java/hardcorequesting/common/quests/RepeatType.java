package hardcorequesting.common.quests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;

public enum RepeatType {
    NONE("none", false) {
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getMessage(Quest quest, Player player, int days, int hours) {
            return Collections.emptyList();
        }
    },
    INSTANT("instant", false) {
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getMessage(Quest quest, Player player, int days, int hours) {
            return  ImmutableList.of(getMessageTitle(),
                    Translator.translatable("hqm.repeat.instant.message").withStyle(ChatFormatting.DARK_GRAY));
        }
    
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getShortMessage(int days, int hours) {
            return ImmutableList.of(Translator.translatable("hqm.repeat.instant.message").withStyle(ChatFormatting.YELLOW));
        }
    },
    INTERVAL("interval", true) {
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getMessage(Quest quest, Player player, int days, int hours) {
            return  ImmutableList.of(getMessageTitle(),
                    Translator.translatable("hqm.repeat.interval.message").withStyle(ChatFormatting.DARK_GRAY),
                    formatTime(days, hours),
                    formatIntervalTime(quest, player, days, hours));
        }
    
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getShortMessage(int days, int hours) {
            return ImmutableList.of(Translator.translatable("hqm.repeat.interval.message")
                    .append(" (" + days + ":" + hours + ")").withStyle(ChatFormatting.YELLOW));
        }
    },
    TIME("time", true) {
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getMessage(Quest quest, Player player, int days, int hours) {
            List<FormattedText> tooltip = Lists.newArrayList(getMessageTitle(),
                    Translator.translatable("hqm.repeat.time.message").withStyle(ChatFormatting.DARK_GRAY),
                    formatTime(days, hours));
            
            QuestData data = quest.getQuestData(player);
            if (!data.available) {
                tooltip.add(formatCooldownTime(quest, player, data, days, hours));
            }
            
            return tooltip;
        }
    
        @Environment(EnvType.CLIENT)
        @Override
        public List<FormattedText> getShortMessage(int days, int hours) {
            return ImmutableList.of(Translator.translatable("hqm.repeat.time.message")
                    .append(" (" + days + ":" + hours + ")").withStyle(ChatFormatting.YELLOW));
        }
    };
    
    private final String id;
    private final boolean useTime;
    
    RepeatType(String id, boolean useTime) {
        this.id = id;
        this.useTime = useTime;
    }
    
    @Environment(EnvType.CLIENT)
    private static FormattedText formatCooldownTime(Quest quest, Player player, QuestData data, int days, int hours) {
        int timerDuration = days * 24 + hours;
        long timerStart = data.time;
        long current = Quest.clientTicker.getHours();
        int remaining = (int) (timerStart + timerDuration - current);
        
        return formatRemainingTime(quest, player, remaining / 24, remaining % 24);
    }
    
    /**
     * Formats and produces text describing the reset time, given the interval time between scheduled resets
     */
    @Environment(EnvType.CLIENT)
    private static FormattedText formatIntervalTime(Quest quest, Player player, int days, int hours) {
        if (days == 0 && hours == 0) {
            return Translator.translatable("hqm.repeat.invalid").withStyle(ChatFormatting.DARK_RED);
        }
        
        int interval = days * 24 + hours;
        int remaining = interval - (int) (Quest.clientTicker.getHours() % interval);
        
        return formatRemainingTime(quest, player, remaining / 24, remaining % 24);
    }
    
    /**
     * Formats and produces text describing the reset time, given the remaining time until the next reset
     */
    @Environment(EnvType.CLIENT)
    private static FormattedText formatRemainingTime(Quest quest, Player player, int days, int hours) {
        
        if (!quest.isAvailable(player)) {
            return Translator.translatable("hqm.repeat.resetIn", formatTime(days, hours)).withStyle(ChatFormatting.YELLOW);
        } else {
            return Translator.translatable("hqm.repeat.nextReset", formatTime(days, hours)).withStyle(ChatFormatting.DARK_GRAY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private static FormattedText formatTime(int days, int hours) {
        MutableComponent daysComp = Translator.text(days + " ").append(Translator.translatable("hqm.repeat." + (days == 1 ? "day" : "days")));
        if (days > 0) {
            daysComp.withStyle(ChatFormatting.GRAY);
        }
        
        MutableComponent hoursComp = Translator.text(hours + " ").append(Translator.translatable("hqm.repeat." + (hours == 1 ? "hour" : "hours")));
        if (hours > 0) {
            hoursComp.withStyle(ChatFormatting.GRAY);
        }
        
        //TODO tweak lang stuff and use formatting for nicer text building
        return Translator.text("").append(daysComp).append(" ").append(Translator.translatable("hqm.repeat.and"))
                .append(" ").append(hoursComp).withStyle(ChatFormatting.DARK_GRAY);
    }
    
    public FormattedText getName() {
        return Translator.translatable("hqm.repeat." + id + ".title");
    }
    
    public FormattedText getDescription() {
        return Translator.translatable("hqm.repeat." + id + ".desc");
    }
    
    public boolean isUseTime() {
        return useTime;
    }
    
    private static FormattedText getMessageTitle() {
        return Translator.translatable("hqm.repeat.repeatable").withStyle(ChatFormatting.YELLOW);
    }
    
    @Environment(EnvType.CLIENT)
    public abstract List<FormattedText> getMessage(Quest quest, Player player, int days, int hours);
    
    @Environment(EnvType.CLIENT)
    public List<FormattedText> getShortMessage(int days, int hours) {
        return Collections.emptyList();
    }
}
