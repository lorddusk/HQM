package hardcorequesting.common.proxies;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestTicker;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class CommonProxy {
    public void init() {
        Quest.serverTicker = new QuestTicker();
        HardcoreQuestingCore.platform.registerOnServerTick(minecraftServer -> Quest.serverTicker.tick(minecraftServer.overworld(), false));
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
    
    /**
     * @return the registries to use when (de)serializing registry-backed data such as enchantments, or {@code null} if none are available yet
     */
    @Nullable
    public RegistryAccess registryAccess() {
        MinecraftServer server = HardcoreQuestingCore.getServer();
        return server != null ? server.registryAccess() : null;
    }
}
