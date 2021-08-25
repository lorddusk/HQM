package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class ResourceHelper {
    
    private ResourceHelper() {
    }
    
    public static ResourceLocation getResource(String name) {
        return new ResourceLocation("hardcorequesting", "textures/gui/" + name + ".png");
    }
    
    public static void bindResource(ResourceLocation resource) {
        RenderSystem.setShaderTexture(0, resource);
    }
    
}
