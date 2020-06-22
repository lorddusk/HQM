package hardcorequesting.team;

import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;

public enum RewardSetting {
    ALL("hqm.team.allReward.title", "hqm.team.allReward.desc"),
    ANY("hqm.team.anyReward.title", "hqm.team.anyReward.desc"),
    RANDOM("hqm.team.randomReward.title", "hqm.team.randomReward.desc");
    
    public static boolean isAllModeEnabled;
    private String title;
    private String description;
    
    RewardSetting(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public static RewardSetting getDefault() {
        return isAllModeEnabled ? ALL : ANY;
    }
    
    @Environment(EnvType.CLIENT)
    public String getTitle() {
        return I18n.translate(title);
    }
    
    @Environment(EnvType.CLIENT)
    public String getDescription() {
        return I18n.translate(description);
    }
}
