package hardcorequesting.bag;


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

    @Override
    public String toString() {
        return super.toString().charAt(0) + super.toString().substring(1).toLowerCase();
    }
}
