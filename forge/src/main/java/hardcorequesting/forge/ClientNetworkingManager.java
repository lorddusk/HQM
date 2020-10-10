package hardcorequesting.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
public class ClientNetworkingManager {
    public static void initClient() {
        NetworkingManager.CHANNEL.addListener(NetworkingManager.createPacketHandler(NetworkEvent.ServerCustomPayloadEvent.class, NetworkingManager.S2C));
    }
    
    public static PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
