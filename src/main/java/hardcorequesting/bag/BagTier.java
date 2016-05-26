package hardcorequesting.bag;


import hardcorequesting.util.Translator;
import hardcorequesting.client.interfaces.GuiColor;

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

    public String getName() {
        return Translator.translate("hqm.bag." + this.name().toLowerCase());
    }

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
