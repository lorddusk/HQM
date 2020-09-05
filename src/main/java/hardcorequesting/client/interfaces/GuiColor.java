package hardcorequesting.client.interfaces;

public enum GuiColor {
    BLACK(0, 0x000000),
    BLUE(1, 0x0000aa),
    GREEN(2, 0x00aa00),
    CYAN(3, 0x00aaaa),
    RED(4, 0xff5555),
    PURPLE(5, 0xaa00aa),
    ORANGE(6, 0xffaa00),
    LIGHT_GRAY(7, 0x555555),
    GRAY(8, 0xaaaaaa),
    LIGHT_BLUE(9, 0x3c7ecf),
    LIME(10, 0x55ff55),
    TURQUOISE(11, 0x40e0d0),
    PINK(12, 0xff55ff),
    MAGENTA(13, 0xff00ff),
    YELLOW(14, 0xffff55),
    WHITE(15, 0xffffff);
    
    
    private String tagColor;
    private float red;
    private float green;
    private float blue;
    private int hex;
    
    GuiColor(int number, float red, float green, float blue) {
        this.tagColor = "\u00a7" + Integer.toHexString(number);
        this.red = red;
        this.green = green;
        this.blue = blue;
        
        hex = (0xFF << 24) | ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | ((int) (blue * 255));
    }
    
    GuiColor(int number, int hex) {
        this.tagColor = "\u00a7" + Integer.toHexString(number);
        this.hex = hex;
        
        this.red = hex >> 16 & 0xFF;
        this.green = hex >> 8 & 0xFF;
        this.blue = hex & 0xFF;
    }
    
    public float getRed() {
        return red;
    }
    
    public float getGreen() {
        return green;
    }
    
    public float getBlue() {
        return blue;
    }
    
    public int getHexColor() {
        return hex;
    }
    
    @Override
    public String toString() {
        return tagColor;
    }
    
    public String getName() {
        return (super.toString().charAt(0) + super.toString().substring(1).toLowerCase()).replace("_", " ");
    }
}

