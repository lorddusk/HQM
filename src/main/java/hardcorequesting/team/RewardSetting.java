package hardcorequesting.team;

import hardcorequesting.util.Translator;

public enum RewardSetting {
    ALL("hqm.team.allReward.title", "hqm.team.allReward.desc"),
    ANY("hqm.team.anyReward.title", "hqm.team.anyReward.desc"),
    RANDOM("hqm.team.randomReward.title", "hqm.team.randomReward.desc");

    public static RewardSetting getDefault() {
        return isAllModeEnabled ? ALL : ANY;
    }

    private String title;
    private String description;

    RewardSetting(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return Translator.translate(title);
    }

    public String getDescription() {
        return Translator.translate(description);
    }

    public static boolean isAllModeEnabled;
}
