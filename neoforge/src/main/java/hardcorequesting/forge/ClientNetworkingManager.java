package hardcorequesting.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientNetworkingManager {
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
