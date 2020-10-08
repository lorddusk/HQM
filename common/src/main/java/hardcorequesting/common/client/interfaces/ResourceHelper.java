package hardcorequesting.common.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class ResourceHelper {
    
    private ResourceHelper() {
    }
    
    public static ResourceLocation getResource(String name) {
        return new ResourceLocation("hardcorequesting", "textures/gui/" + name + ".png");
    }
    
    public static void bindResource(ResourceLocation resource) {
        Minecraft.getInstance().getTextureManager().bind(resource);
    }
    
}
