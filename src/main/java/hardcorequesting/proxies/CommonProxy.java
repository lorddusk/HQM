package hardcorequesting.proxies;

import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;

public class CommonProxy {
    public void initSounds(Path path) {
        
    }
    
    public void init() {
        Quest.serverTicker = new QuestTicker(false);
    }
    
    public boolean isClient() {
        return false;
    }
    
    public boolean isServer() {
        return true;
    }
    
    public Player getPlayer(PacketContext ctx) {
        return ctx.getPlayer();
    }
}
