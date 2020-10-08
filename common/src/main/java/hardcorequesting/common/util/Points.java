package hardcorequesting.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;

@Environment(EnvType.CLIENT)
public final class Points {
    private Points() {}
    
    public static Vec2 ofMouse() {
        Minecraft client = Minecraft.getInstance();
        double mx = client.mouseHandler.xpos() * (double) client.getWindow().getGuiScaledWidth() / (double) client.getWindow().getWidth();
        double my = client.mouseHandler.ypos() * (double) client.getWindow().getGuiScaledHeight() / (double) client.getWindow().getHeight();
        return new Vec2((float) mx, (float) my);
    }
}