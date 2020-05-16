package hardcorequesting.client.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public abstract class ResourceHelper {
    
    private ResourceHelper() {
    }
    
    public static Identifier getResource(String name) {
        return new Identifier("hardcorequesting", "textures/gui/" + name + ".png");
    }
    
    public static void bindResource(Identifier resource) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(resource);
    }
    
}
