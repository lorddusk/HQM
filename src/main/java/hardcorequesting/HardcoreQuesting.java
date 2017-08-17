package hardcorequesting;

import java.io.File;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.ConfigHandler;
import hardcorequesting.event.PlayerDeathEventListener;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.event.WorldEventListener;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ModInformation.ID, name = ModInformation.NAME, version = ModInformation.VERSION, guiFactory = "hardcorequesting.client.interfaces.HQMModGuiFactory")
public class HardcoreQuesting {

    @Instance(ModInformation.ID)
    public static HardcoreQuesting instance;

    @SidedProxy(clientSide = "hardcorequesting.proxies.ClientProxy", serverSide = "hardcorequesting.proxies.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs HQMTab = new HQMTab();

    public static String path;

    public static File configDir;

    public static Side loadingSide;

    private static EntityPlayer commandUser;

    public static EntityPlayer getPlayer() {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player) {
        commandUser = player;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        loadingSide = event.getSide();
        new hardcorequesting.event.EventHandler();

        path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + ModInformation.CONFIG_LOC_NAME.toLowerCase() + File.separator;
        configDir = new File(path);
        ConfigHandler.initModConfig(path);
        ConfigHandler.initEditConfig(path);
        QuestLine.init(path);

        proxy.init();
        proxy.initSounds(path);

        ModBlocks.init();
        ModBlocks.registerTileEntities();

        ModItems.init();
        
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();

        NetworkManager.init();
        proxy.initRenderers();

        ModItems.registerRecipes();
        ModBlocks.registerRecipes();

        FMLInterModComms.sendMessage("Waila", "register", "hardcorequesting.waila.Provider.callbackRegister");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(CommandHandler.instance);
    }
    
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        RegisterHelper.registerBlocks(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        RegisterHelper.registerItems(event);
    }
    
    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        Sounds.registerSounds(event);
    }
}
