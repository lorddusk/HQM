package hardcorequesting.common.bag;


import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum BagTier {
    BASIC(ChatFormatting.DARK_GRAY),
    GOOD(ChatFormatting.DARK_GREEN),
    GREATER(ChatFormatting.DARK_BLUE),
    EPIC(ChatFormatting.GOLD),
    LEGENDARY(ChatFormatting.DARK_PURPLE);
    
    
    private final ChatFormatting color;
    
    BagTier(ChatFormatting color) {
        this.color = color;
    }
    
    public ChatFormatting getColor() {
        return color;
    }
    
    @Environment(EnvType.CLIENT)
    public Component getColoredName() {
        return Translator.translatable("hqm.bag." + this.name().toLowerCase()).withStyle(color);
    }
    
    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
