package hardcorequesting.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * @author canitzp
 * @since 5.4.0
 */
public class HQMUtil{
    
    /**
     * A true and save way to determine if a player is actually in a single player only game. No Server nor Integrated Server (LAN World)
     *
     * @param world The world of the player
     * @return true if the player plays in a real single player world
     */
    public static boolean isGameSingleplayer(@Nonnull World world){
        MinecraftServer server = world.getMinecraftServer();
        return server == null || (server instanceof IntegratedServer && !(((IntegratedServer) server).getPublic()));
    }
    
}
