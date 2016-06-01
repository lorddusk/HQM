package hardcorequesting;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.ConfigHandler;
import hardcorequesting.event.PlayerDeathEventListener;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.event.WorldEventListener;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.QuestLine;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;

@Mod(modid = ModInformation.ID, name = ModInformation.NAME, version = ModInformation.VERSION, guiFactory = "hardcorequesting.client.interfaces.HQMModGuiFactory")
public class HardcoreQuesting {

    @Instance(ModInformation.ID)
    public static HardcoreQuesting instance;

    @SidedProxy(clientSide = "hardcorequesting.proxies.ClientProxy", serverSide = "hardcorequesting.proxies.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs HQMTab = new HQMTab();

    public static String path;

    public static File configDir;

    private static EntityPlayer commandUser;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        new hardcorequesting.event.EventHandler();

        path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + ModInformation.CONFIG_LOC_NAME.toLowerCase() + File.separator;
        configDir = new File(path);
        ConfigHandler.initModConfig(path);
        ConfigHandler.initEditConfig(path);
        QuestLine.init(path + File.separator + SaveHandler.REMOTE, event.getSide().isClient()); // Init Quest line within config folder used for server connections

        proxy.init();
        proxy.initSounds(path);

        ModBlocks.init();
        ModBlocks.registerTileEntities();

        ModItems.init();
        proxy.initRenderers();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);

        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();

        NetworkManager.init();

        ModItems.registerRecipes();
        ModBlocks.registerRecipes();

        FMLInterModComms.sendMessage("Waila", "register", "hardcorequesting.waila.Provider.callbackRegister");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(CommandHandler.instance);
    }


    public static EntityPlayer getPlayer() {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player) {
        commandUser = player;
    }
}
