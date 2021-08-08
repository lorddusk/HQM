package hardcorequesting.common.proxies;

import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy extends CommonProxy {
    @Override
    public void initSounds() {
        Sounds.initSounds();
    }
    
    @Override
    public void init() {
        super.init();
        Quest.clientTicker = QuestTicker.initClientTicker();
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    @Override
    public boolean isServer() {
        return false;
    }
    
    @Override
    public Player getPlayer(PacketContext ctx) {
        return ctx.isClient() ? Minecraft.getInstance().player : super.getPlayer(ctx);
    }
}
