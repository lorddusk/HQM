package hardcorequesting.common.team;

import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;

public enum LifeSetting {
    SHARE("hqm.team.sharedLives.title", "hqm.team.sharedLives.desc"),
    INDIVIDUAL("hqm.team.individualLives.title", "hqm.team.individualLives.desc");
    
    private final String title;
    private final String description;
    
    LifeSetting(String title, String description) {
        this.title = title;
        this.description = description;
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
