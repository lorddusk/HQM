package hardcorequesting.team;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;

public enum LifeSetting {
    SHARE("hqm.team.sharedLives.title", "hqm.team.sharedLives.desc"),
    INDIVIDUAL("hqm.team.individualLives.title", "hqm.team.individualLives.desc");
    
    private String title;
    private String description;
    
    LifeSetting(String title, String description) {
        this.title = title;
        this.description = description;
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
