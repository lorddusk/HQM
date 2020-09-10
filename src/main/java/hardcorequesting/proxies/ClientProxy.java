package hardcorequesting.proxies;

import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;


public class ClientProxy extends CommonProxy {
    @Override
    public void initSounds() {
        Sounds.initSounds();
    }
    
    @Override
    public void init() {
        super.init();
        Quest.clientTicker = new QuestTicker(true);
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
        return ctx.getPacketEnvironment() == EnvType.CLIENT ? Minecraft.getInstance().player : super.getPlayer(ctx);
    }
}
