package hardcorequesting.tileentity;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.items.ModItems;
import hardcorequesting.util.Translator;
import net.minecraft.item.ItemStack;

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
    
    public ItemStack createItemStack(){
        return this.isPreset() ? new ItemStack(ModBlocks.itemPortal, 1, this.ordinal() + 1) : ItemStack.EMPTY;
    }

    public boolean isPreset() {
        return isPreset;
    }
}
