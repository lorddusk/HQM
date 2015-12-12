package hardcorequesting.tileentity;

import hardcorequesting.Translator;

public enum PortalType {
    TECH("tech", true),
    MAGIC("magic", true),
    CUSTOM("custom", false);

    private String id;
    private boolean isPreset;

    PortalType(String id, boolean isPreset) {
        this.id = id;
        this.isPreset = isPreset;
    }

    public String getName() {
        return Translator.translate("hqm.portal." + this.id + ".title");
    }

    public String getDescription() {
        return Translator.translate("hqm.portal." + this.id + ".desc");
    }

    public boolean isPreset() {
        return isPreset;
    }
}
