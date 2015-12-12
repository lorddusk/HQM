package hardcorequesting.client.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public abstract class ResourceHelper {

    public static ResourceLocation getResource(String name) {
        return new ResourceLocation("hqm", "textures/gui/" + name + ".png");
    }

    public static void bindResource(ResourceLocation resource) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
    }

    private ResourceHelper() {
    }

}
