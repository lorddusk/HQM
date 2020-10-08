package hardcorequesting.common.tileentity;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

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
        return I18n.get("hqm.portal." + this.id + ".title");
    }
    
    public String getDescription() {
        return I18n.get("hqm.portal." + this.id + ".desc");
    }
    
    public ItemStack createItemStack() {
        throw new AssertionError();
//        return this.isPreset() ? new ItemStack(ModBlocks.blockPortal, 1, this.ordinal() + 1) : ItemStack.EMPTY;
    }
    
    public boolean isPreset() {
        return isPreset;
    }
}
