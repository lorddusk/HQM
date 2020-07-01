package hardcorequesting.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
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
