package hardcorequesting.bag;


import hardcorequesting.client.interfaces.GuiColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

public enum BagTier {
    BASIC(GuiColor.GRAY),
    GOOD(GuiColor.GREEN),
    GREATER(GuiColor.BLUE),
    EPIC(GuiColor.ORANGE),
    LEGENDARY(GuiColor.PURPLE);
    
    
    private GuiColor color;
    
    BagTier(GuiColor color) {
        this.color = color;
    }
    
    public GuiColor getColor() {
        return color;
    }
    
    @Environment(EnvType.CLIENT)
    public String getName() {
        return I18n.get("hqm.bag." + this.name().toLowerCase());
    }
    
    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
