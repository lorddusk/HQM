package hardcorequesting.common;

import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.event.PlayerDeathEventListener;
import hardcorequesting.common.event.PlayerTracker;
import hardcorequesting.common.event.WorldEventListener;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.proxies.ClientProxy;
import hardcorequesting.common.proxies.CommonProxy;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.util.Executor;
import hardcorequesting.common.util.RegisterHelper;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class HardcoreQuestingCore {
    public static final String ID = "hardcorequesting";
    
    public static CommonProxy proxy;
    
    public static Path configDir;
    public static Path packDir;
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("Hardcore Questing Mode");
    
    public static AbstractPlatform platform;
    
    public static MinecraftServer getServer() {
        return platform == null ? null : platform.getServer();
    }
    
    public static void initialize(AbstractPlatform platform) {
        HardcoreQuestingCore.platform = platform;
        HardcoreQuestingCore.proxy = Executor.call(() -> ClientProxy::new, () -> CommonProxy::new);
        
        new EventTrigger();
        
        configDir = platform.getConfigDir().resolve("hqm");
        packDir = configDir.resolve("default");
        try {
            FileUtils.deleteDirectory(configDir.resolve("remote").toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        QuestLine.reset();
        
        HQMConfig.loadConfig();
        
        proxy.init();
        
        new PlayerDeathEventListener();
        new PlayerTracker();
        
        NetworkManager.init();
        RegisterHelper.register();
        Sounds.registerSounds();
        
        platform.registerOnCommandRegistration(CommandHandler::register);
        platform.registerOnWorldLoad(WorldEventListener::onLoad);
        platform.registerOnWorldSave(WorldEventListener::onSave);
        platform.registerOnPlayerJoin((playerEntity) -> {
            PlayerTracker.instance.onPlayerLogin(playerEntity);
            EventTrigger.instance().onPlayerLogin(playerEntity);
        });
        platform.registerOnLivingDeath((livingEntity, source) -> {
            PlayerDeathEventListener.instance.onLivingDeath(livingEntity, source);
            EventTrigger.instance().onLivingDeath(livingEntity, source);
        });
    }
}
