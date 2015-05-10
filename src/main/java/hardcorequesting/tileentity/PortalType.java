package hardcorequesting.tileentity;


public enum PortalType {
    TECH("Tech Theme", "This set works perfectly for your technical themed map.", true),
    MAGIC("Magic Theme", "This set works perfectly for your magical themed map.", true),
    CUSTOM("Custom Theme", "Customize the theme by specify what block this block should look like.", false);

    private String name;
    private String description;
    private boolean isPreset;

    PortalType(String name, String description, boolean isPreset) {
        this.name = name;
        this.description = description;
        this.isPreset = isPreset;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPreset() {
        return isPreset;
    }
}
