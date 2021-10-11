package hardcorequesting.common.bag;

import net.minecraft.network.chat.TextColor;

public enum TierColor {
    BLACK(0x000000),
    BLUE(0x0000aa),
    GREEN(0x00aa00),
    CYAN(0x00aaaa),
    RED(0xff5555),
    PURPLE(0xaa00aa),
    ORANGE(0xffaa00),
    LIGHT_GRAY(0x555555),
    GRAY(0xaaaaaa),
    LIGHT_BLUE(0x3c7ecf),
    LIME(0x55ff55),
    TURQUOISE(0x40e0d0),
    PINK(0xff55ff),
    MAGENTA(0xff00ff),
    YELLOW(0xffff55),
    WHITE(0xffffff);
    
    private final TextColor color;
    
    TierColor(int hex) {
        this.color = TextColor.fromRgb(hex);
    }
    
    public int getHexColor() {
        return color.getValue();
    }
    
    public String getName() {
        return (super.toString().charAt(0) + super.toString().substring(1).toLowerCase()).replace("_", " ");
    }
}

