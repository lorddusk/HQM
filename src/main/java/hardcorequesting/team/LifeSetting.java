package hardcorequesting.team;

import hardcorequesting.util.Translator;

public enum LifeSetting {
    SHARE("hqm.team.sharedLives.title", "hqm.team.sharedLives.desc"),
    INDIVIDUAL("hqm.team.individualLives.title", "hqm.team.individualLives.desc");
    
    private String title;
    private String description;
    
    LifeSetting(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public String getTitle() {
        return Translator.translate(title);
    }
    
    public String getDescription() {
        return Translator.translate(description);
    }
}
