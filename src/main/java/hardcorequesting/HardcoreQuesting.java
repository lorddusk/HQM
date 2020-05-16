package hardcorequesting;

import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.event.PlayerDeathEventListener;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.event.WorldEventListener;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.proxies.ClientProxy;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.Executor;
import hardcorequesting.util.RegisterHelper;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class HardcoreQuesting implements ModInitializer {
    
    public static final String ID = "hardcorequesting";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "hcQuesting";
    public static final String CONFIG_LOC_NAME = "hqm";
    public static final String SOUNDLOC = "hardcorequesting";
    
    public static HardcoreQuesting instance;
    
    @SuppressWarnings("Convert2MethodRef")
    public static CommonProxy proxy = Executor.call(() -> () -> new ClientProxy(), () -> () -> new CommonProxy());
    public static ItemGroup HQMTab = FabricItemGroupBuilder.create(new Identifier("hardcorequesting", "hardcorequesting"))
            .icon(() -> new ItemStack(ModItems.book))
            .build();
    
    public static String path;
    
    public static File configDir;
    
    public static EnvType loadingSide;
    
    public static final String NAME = "Hardcore Questing Mode";
    public static final Logger LOG = LogManager.getFormatterLogger(NAME);
    
    private static PlayerEntity commandUser;
    
    public static PlayerEntity getPlayer() {
        return commandUser;
    }
    
    public static void setPlayer(PlayerEntity player) {
        commandUser = player;
    }
    
    public HardcoreQuesting() {
        HardcoreQuesting.instance = this;
    }
    
    public static MinecraftServer getServer() {
        return proxy.getServerInstance();
    }
    
    public static final ComponentType<CompoundTagComponent> PLAYER_EXTRA_DATA;
    
    static {
        PLAYER_EXTRA_DATA = ComponentRegistry.INSTANCE
                .registerIfAbsent(new Identifier(ID, "player_extra_data"), CompoundTagComponent.class)
                .attach(EntityComponentCallback.event(PlayerEntity.class), player -> new CompoundTagComponent());
        EntityComponents.setRespawnCopyStrategy(PLAYER_EXTRA_DATA, RespawnCopyStrategy.ALWAYS_COPY);
    }
    
    public static class CompoundTagComponent implements Component {
        public CompoundTag tag = new CompoundTag();
        
        @Override
        public void fromTag(CompoundTag tag) {
            this.tag = tag.getCompound("Tag");
        }
        
        @NotNull
        @Override
        public CompoundTag toTag(CompoundTag tag) {
            this.tag.put("Tag", this.tag);
            return tag;
        }
    }
    
    @Override
    public void onInitialize() {
        loadingSide = FabricLoader.getInstance().getEnvironmentType();
        new EventTrigger();
        
        path = FabricLoader.getInstance().getConfigDirectory().getAbsolutePath() + File.separator + CONFIG_LOC_NAME.toLowerCase() + File.separator;
        configDir = new File(path);
        QuestLine.init(path);
        
        HQMConfig.loadConfig();
        
        proxy.init();
        proxy.initSounds(path);
        
        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();
        
        NetworkManager.init();
        proxy.initRenderers();
        
        RegisterHelper.register();
        Sounds.registerSounds();
        
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CommandHandler.register(dispatcher));
    }
    
    public void serverStarting(MinecraftServer server) {
//        event.registerServerCommand(CommandHandler.instance);
    }
}
