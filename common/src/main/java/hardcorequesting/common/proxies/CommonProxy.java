package hardcorequesting.common.proxies;

import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestTicker;
import net.minecraft.world.entity.player.Player;

public class CommonProxy {
    public void initSounds() {}
    
    public void init() {
        Quest.serverTicker = QuestTicker.initServerTicker();
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
