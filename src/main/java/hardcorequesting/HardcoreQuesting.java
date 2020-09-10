package hardcorequesting;

import hardcorequesting.capabilities.ModCapabilities;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.event.PlayerDeathEventListener;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.event.WorldEventListener;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.proxies.ClientProxy;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.Executor;
import hardcorequesting.util.RegisterHelper;
import me.shedaniel.cloth.api.common.events.v1.PlayerJoinCallback;
import me.shedaniel.cloth.api.common.events.v1.PlayerLeaveCallback;
import me.shedaniel.cloth.api.common.events.v1.WorldLoadCallback;
import me.shedaniel.cloth.api.common.events.v1.WorldSaveCallback;
import me.shedaniel.cloth.api.utils.v1.GameInstanceUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class HardcoreQuesting implements ModInitializer {
    public static final String ID = "hardcorequesting";
    public static final EnvType LOADING_SIDE = FabricLoader.getInstance().getEnvironmentType();
    
    @SuppressWarnings("Convert2MethodRef")
    public static CommonProxy proxy = Executor.call(() -> () -> new ClientProxy(), () -> () -> new CommonProxy());
    
    public static Path configDir;
    public static Path dataDir;
    public static Path packDir;
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("Hardcore Questing Mode");
    
    public static MinecraftServer getServer() {
        return GameInstanceUtils.getServer();
    }
    
    @Override
    public void onInitialize() {
        ModCapabilities.init();
        new EventTrigger();
        
        configDir = FabricLoader.getInstance().getConfigDir().resolve("hqm");
        packDir = configDir.resolve("default");
        dataDir = configDir.resolve("data");
        try {
            FileUtils.deleteDirectory(configDir.resolve("remote").toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        QuestLine.reset(Optional.of(packDir), Optional.of(dataDir)).loadAll();
        
        HQMConfig.loadConfig();
        
        proxy.init();
        proxy.initSounds();
        
        new PlayerDeathEventListener();
        new PlayerTracker();
        
        NetworkManager.init();
        RegisterHelper.register();
        Sounds.registerSounds();
        
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CommandHandler.register(dispatcher));
        WorldLoadCallback.EVENT.register(WorldEventListener::onLoad);
        WorldSaveCallback.EVENT.register(WorldEventListener::onSave);
        PlayerJoinCallback.EVENT.register((connection, playerEntity) -> {
            PlayerTracker.instance.onPlayerLogin(playerEntity);
            EventTrigger.instance().onPlayerLogin(playerEntity);
        });
    }
}
