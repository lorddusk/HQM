package hardcorequesting.common.network;

import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public interface PacketContext {
    Player getPlayer();
    
    Consumer<Runnable> getTaskQueue();
    
    boolean isClient();
}
