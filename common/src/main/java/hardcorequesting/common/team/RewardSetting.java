package hardcorequesting.common.team;

import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;

public enum RewardSetting {
    ALL("hqm.team.allReward.title", "hqm.team.allReward.desc"),
    ANY("hqm.team.anyReward.title", "hqm.team.anyReward.desc"),
    RANDOM("hqm.team.randomReward.title", "hqm.team.randomReward.desc");
    
    public static boolean isAllModeEnabled;
    private final String title;
    private final String description;
    
    RewardSetting(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public static RewardSetting getDefault() {
        return isAllModeEnabled ? ALL : ANY;
    }
    
    @Environment(EnvType.CLIENT)
    public FormattedText getTitle() {
        return Translator.translatable(title).withStyle(ChatFormatting.DARK_GREEN);
    }
    
    @Environment(EnvType.CLIENT)
    public FormattedText getDescription() {
        return Translator.translatable(description).withStyle(ChatFormatting.DARK_GREEN);
    }
}
